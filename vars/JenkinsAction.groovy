import java.security.*

def call(def options) {
	// pull actionImage
	sh "docker pull ${options.action}"
	def actionArgs = sh(returnStdout: true,
		script: "docker inspect ${options.action} -f '{{ .Config.Labels.ACTION_ARGS }}'" ).trim()

	// execute action
	stage("${options.name}") {
		// actions args
		def entrypointArgs = ""
		actionArgs.split(",").each { argName ->
			entrypointArgs += "\"${options.args[argName]}\""
		}

		// run action
		sh "docker run --rm ${options.action} ${entrypointArgs}"
	}
}
