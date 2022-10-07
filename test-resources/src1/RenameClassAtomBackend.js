// Private: Emulate a "filesystem watcher" by subscribing to Atom events like buffers being saved. This will miss
// any changes made to files outside of Atom, but it also has no overhead.
class AtomBackend {
  async start (rootPath, eventCallback, errorCallback) {
    const getRealPath = givenPath => {
      return new Promise(resolve => {
        fs.realpath(givenPath, (err, resolvedPath) => {
          err ? resolve(null) : resolve(resolvedPath)
        })
      })
    }

    this.subs = new CompositeDisposable()

    this.subs.add(atom.workspace.observeTextEditors(async editor => {
      let realPath = await getRealPath(editor.getPath())
      if (!realPath || !realPath.startsWith(rootPath)) {
        return
      }

      const announce = (action, oldPath) => {
        const payload = {action, path: realPath}
        if (oldPath) payload.oldPath = oldPath
        eventCallback([payload])
      }

      const buffer = editor.getBuffer()

      this.subs.add(buffer.onDidConflict(() => announce('modified')))
      this.subs.add(buffer.onDidReload(() => announce('modified')))
      this.subs.add(buffer.onDidSave(event => {
        if (event.path === realPath) {
          announce('modified')
        } else {
          const oldPath = realPath
          realPath = event.path
          announce('renamed', oldPath)
        }
      }))

      this.subs.add(buffer.onDidDelete(() => announce('deleted')))

      this.subs.add(buffer.onDidChangePath(newPath => {
        if (newPath !== realPath) {
          const oldPath = realPath
          realPath = newPath
          announce('renamed', oldPath)
        }
      }))
    }))

    // Giant-ass brittle hack to hook files (and eventually directories) created from the TreeView.
    const treeViewPackage = await atom.packages.getLoadedPackage('tree-view')
    if (!treeViewPackage) return
    await treeViewPackage.activationPromise
    const treeViewModule = treeViewPackage.mainModule
    if (!treeViewModule) return
    const treeView = treeViewModule.getTreeViewInstance()

    const isOpenInEditor = async eventPath => {
      const openPaths = await Promise.all(
        atom.workspace.getTextEditors().map(editor => getRealPath(editor.getPath()))
      )
      return openPaths.includes(eventPath)
    }

    this.subs.add(treeView.onFileCreated(async event => {
      const realPath = await getRealPath(event.path)
      if (!realPath) return

      eventCallback([{action: 'added', path: realPath}])
    }))

    this.subs.add(treeView.onEntryDeleted(async event => {
      const realPath = await getRealPath(event.path)
      if (!realPath || isOpenInEditor(realPath)) return

      eventCallback([{action: 'deleted', path: realPath}])
    }))

    this.subs.add(treeView.onEntryMoved(async event => {
      const [realNewPath, realOldPath] = await Promise.all([
        getRealPath(event.newPath),
        getRealPath(event.initialPath)
      ])
      if (!realNewPath || !realOldPath || isOpenInEditor(realNewPath) || isOpenInEditor(realOldPath)) return

      eventCallback([{action: 'renamed', path: realNewPath, oldPath: realOldPath}])
    }))
  }

  async stop () {
    this.subs && this.subs.dispose()
  }
}