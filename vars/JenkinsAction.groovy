import java.security.*

def call(def options) {
	// pull actionImage
	sh "docker pull ${options.action}"
	def actionEntrypoint = sh(returnStdout: true,
		script: "docker inspect ${options.action} -f '{{ .Config.Labels.ACTION_ENTRYPOINT }}'" ).trim()
	def actionArgs = sh(returnStdout: true,
		script: "docker inspect ${options.action} -f '{{ .Config.Labels.ACTION_ARGS }}'" ).trim()

	// execute action
	stage("${options.name}") {
		// override entrypoint, Jenkins need it.
		def actionRunArgs = "--entrypoint='' "

		// action entrypoint
		def entrypointArgs = ""
		actionArgs.split(",").each { argName ->
			entrypointArgs += "\"${options.args[argName]}\""
		}

		// run action
		withDockerContainer(image: options.action, args: actionRunArgs) {
			sh "${actionEntrypoint} ${entrypointArgs}"
		}
	}
}
