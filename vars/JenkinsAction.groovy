def call(def options) {
	def rand1 = env['BUILD_TAG'].digest('SHA-1').substring(0, 12)
	def dindNetwork = "dind-${rand1}"

	def dindImg = 'docker:18.09.9-dind'
	def dockerImg = "docker:18.09.9"
	def dindArgs = ""
	dindArgs += "--privileged "
	dindArgs += "--network ${dindNetwork} "

	// pull actionImage
	sh "docker pull ${options.action}"
	def actionEntrypoint = sh(returnStdout: true,
		script: "docker inspect ${options.action} -f '{{ .Config.Labels.ACTION_ENTRYPOINT }}'" ).trim()
	def actionArgs = sh(returnStdout: true,
		script: "docker inspect ${options.action} -f '{{ .Config.Labels.ACTION_ARGS }}'" ).trim()

	// execute action
	stage("${options.name}") {
		WithDockerNetwork(dindNetwork) {
			docker.image(dindImg).withRun(dindArgs) { c ->
				def actionRunArgs = ""
				actionRunArgs += "--network ${dindNetwork} "
				actionRunArgs += "-e DOCKER_HOST=tcp://${c.id.substring(0, 12)}:2375 "
				actionRunArgs += "--entrypoint='' "

				def entrypointArgs = ""
				actionArgs.split(",").each { argName ->
					entrypointArgs += "\"${options.args[argName]}\""
				}
				withDockerContainer(image: options.action, args: actionRunArgs) {
					sh "${actionEntrypoint} ${entrypointArgs}"
				}
			}
		}
	}
}
