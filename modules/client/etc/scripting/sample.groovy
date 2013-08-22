def results = coruscli.eval("hosts")

for (hosts in results.collate(1)) {
  for (host in hosts) {
    def target = "${host.endpoint.serverAddress.host}:${host.endpoint.serverAddress.port}"
    println("------ Deploying to: ${target}")
    coruscli.eval("kill all -w -cluster ${target}");
    coruscli.eval("undeploy all -cluster ${target}")
    coruscli.eval("deploy ../server/target/*demo.zip -cluster ${target}")
  }
}

results = coruscli.eval("ls -cluster")
for (r in results) {
  println("------ Distributions for ${r.origin.host}:${r.origin.port}")
  for (dist in r.data) {
    println("${dist.name},${dist.version}");
  }
}

println("completed");
  