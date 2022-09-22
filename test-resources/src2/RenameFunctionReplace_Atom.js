module.exports =
class Project extends Model {
    destroyUnretainedBuffers () {
        for (let buffer of this.getBuffers()) {
          if (!buffer.isRetained()) buffer.destroy()
        }
      }
       // Layers the contents of an atomProject file's config
       // on top of the current global config.
       replace (newSettings) {
         if (newSettings == null) {
           atom.config.clearProjectSettings()
           this.setPaths([])
           this.projectFilePath = null
         } else {
           atom.config.resetProjectSettings(newSettings.config)
           this.projectFilePath = newSettings.originPath
           this.setPaths(newSettings.paths)
         }
         this.emitter.emit('replace', newSettings)
       }

       onDidReplace (callback) {
         return this.emitter.on('replace', callback)
       }

       getProjectFilePath () {
         return this.projectFilePath
       }
};