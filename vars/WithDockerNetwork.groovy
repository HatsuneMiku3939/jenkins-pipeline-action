def call(networkName, body) {
	sh "docker network create ${networkName}"
	try {
		body()
	} finally {
		sh "docker network rm ${networkName}"
	}
}
