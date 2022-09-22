module.exports =
class Project extends Model {
    destroyUnretainedBuffers () {
        for (let buffer of this.getBuffers()) {
          if (!buffer.isRetained()) buffer.destroy()
        }
      }

      // Layers the contents of an atomProject file's config
      // on top of the current global config.
      replaceAtomProject (newSettings) {
        atom.config.resetProjectSettings(newSettings.config)
        this.projectFilePath = newSettings.originPath
        this.setPaths(newSettings.paths)
        this.emitter.emit('replace-atom-project', newSettings)
      }

      onDidReplaceAtomProject (callback) {
        return this.emitter.on('replace-atom-project', callback)
      }

      clearAtomProject () {
        atom.config.clearProjectSettings()
        this.setPaths([])
        this.projectFilePath = null
        this.emitter.emit('replace-atom-project', {})
      }

      getAtomProjectFilePath () {
        return this.projectFilePath
      }
};