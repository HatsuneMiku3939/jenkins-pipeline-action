def call(Map option) {
	def rand1 = Integer.toString(new Random().nextInt(65535) + 1)
	def rand2 = Integer.toString(new Random().nextInt(65535) + 1)
	def dindNetwork = "dind-${rand1}${rand2}"

	def dindImg = 'docker:18.09.9-dind'
	def dockerImg = "docker:18.09.9"
	def dindArgs = ""
	dindArgs += "--privileged "
	dindArgs += "--network ${dindNetwork} "

	// pull actionImage
	sh "docker pull ${option.actionImage}"
	def actionName = sh(returnStdout: true,
		script: "docker inspect ${option.actionImage} -f '{{ .Config.Labels.ACTION_NAME }}'" ).trim()
	def actionEntrypoint = sh(returnStdout: true,
		script: "docker inspect ${option.actionImage} -f '{{ .Config.Labels.ACTION_ENTRYPOINT }}'" ).trim()
	def actionArgs = sh(returnStdout: true,
		script: "docker inspect ${option.actionImage} -f '{{ .Config.Labels.ACTION_ARGS }}'" ).trim()

	// execute action
	stage("${option.name}") {
		WithDockerNetwork(dindNetwork) {
			docker.image(dindImg).withRun(dindArgs) { c ->
				def actionRunArgs = ""
				actionRunArgs += "--network ${dindNetwork} "
				actionRunArgs += "-e DOCKER_HOST=tcp://${c.id.substring(0, 12)}:2375 "
				actionRunArgs += "--entrypoint='' "

				def entrypointArgs = ""
				actionArgs.split(",").each { t->
					entrypointArgs += "\"${env[t]}\" "
				}
				withDockerContainer(image: option.actionImage, args: actionRunArgs) {
					sh "${actionEntrypoint} ${entrypointArgs}"
				}
			}
		}
	}
}
