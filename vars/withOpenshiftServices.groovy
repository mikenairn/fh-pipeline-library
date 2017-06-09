def call(services, body) {
    List<String> names = getNames(services)
    try {
        createOpenshiftResources(services, names)
        withEnv(env(services, names)) {
            body()
        }
    } finally {
        cleanup(names)
    }
}

def cleanup(names) {
    for (int i = 0; i < names.size(); i++) {
        String resourceName = names[i]
        openshiftScale deploymentConfig: resourceName,  replicaCount: 0
        openshift.withCluster() {
            openshift.selector('svc', [name: resourceName]).delete()
            openshift.selector('dc', [name: resourceName]).delete()
        }
    }
}

def createOpenshiftResources(services, names) {
    Map jobs = [:]
    for (int i = 0; i < services.size(); i++) {
        String service = services[i]
        String name = names[i]
        jobs[name] = {
            openshiftCreateResource(getDeploymentConfigYaml(service, name))
            openshiftCreateResource(getServiceYaml(service, name))
            openshiftScale deploymentConfig: name,  replicaCount: 1, verifyReplicaCount: 1, waitTime: 600000
        }
    }
    parallel jobs
}

String sanitizeObjectName(s) {
    s.replaceAll(/[_ ]/, '-')
            .toLowerCase()
            .reverse()
            .take(23)
            .replaceAll("^-+", "")
            .reverse()
            .replaceAll("^-+", "")
}

Map<String, String> getNames(services) {
    List<String> names = []
    for (int i = 0; i < services.size(); i++) {
        names += sanitizeObjectName("${env.BUILD_TAG}-${services[i]}")
    }
    return names
}

List<String> env(services, names) {
    List<String> out = []
    if (services.contains('mongodb')) {
        out.add("MONGODB_HOST=${names[services.indexOf('mongodb')]}")
    }
    if (services.contains('mysql')) {
        out.add("MYSQL_HOST=${names[services.indexOf('mysql')]}")
    }
    if (services.contains('memcached')) {
        out.add("MEMCACHED_HOST=${names[services.indexOf('memcached')]}")
    }
    return out
}

String getDeploymentConfigYaml(service, name) {
    switch (service) {
        case 'mongodb':
        return """
apiVersion: v1
kind: DeploymentConfig
metadata:
  name: ${name}
  labels:
    name: ${name}
spec:
  replicas: 0
  selector:
    name: ${name}
  strategy:
    recreateParams:
      timeoutSeconds: 600
    type: Recreate
  template:
    metadata:
      labels:
        name: ${name}
    spec:
      containers:
      - image: docker.io/mongo:3
        imagePullPolicy: IfNotPresent
        name: mongodb
        ports:
        - containerPort: 27017
          protocol: TCP
        volumeMounts:
        - mountPath: /data/db
          name: data
      dnsPolicy: ClusterFirst
      restartPolicy: Always
      volumes:
      - emptyDir: {}
        name: data
"""
        case 'mysql':
        return """
apiVersion: v1
kind: DeploymentConfig
metadata:
  name: ${name}
  labels:
    name: ${name}
spec:
  replicas: 0
  selector:
    name: ${name}
  strategy:
    recreateParams:
      timeoutSeconds: 600
    type: Recreate
  template:
    metadata:
      labels:
        name: ${name}
    spec:
      containers:
      - env:
        - name: MYSQL_USER
          value: mysql
        - name: MYSQL_PASSWORD
          value: password
        - name: MYSQL_ROOT_PASSWORD
          value: password
        - name: MYSQL_DATABASE
          value: sampledb
        image: docker.io/openshift/mysql-55-centos7
        imagePullPolicy: IfNotPresent
        name: ${name}
        ports:
        - containerPort: 3306
          protocol: TCP
        volumeMounts:
        - mountPath: /var/lib/mysql/data
          name: mysql-data
      dnsPolicy: ClusterFirst
      restartPolicy: Always
      volumes:
      - emptyDir: {}
        name: mysql-data
"""
        case 'memcached':
            return """
apiVersion: v1
kind: DeploymentConfig
metadata:
  name: ${name}
  labels:
    name: ${name}
spec:
  replicas: 0
  selector:
    name: ${name}
  strategy:
    recreateParams:
      timeoutSeconds: 600
    type: Recreate
  template:
    metadata:
      labels:
        name: ${name}
    spec:
      containers:
      - env:
        - name: MYSQL_USER
          value: mysql
        - name: MYSQL_PASSWORD
          value: password
        - name: MYSQL_ROOT_PASSWORD
          value: password
        - name: MYSQL_DATABASE
          value: sampledb
        image: docker.io/rhmap/memcached
        imagePullPolicy: IfNotPresent
        name: ${name}
        ports:
        - containerPort: 11211
          protocol: TCP
        - containerPort: 11211
          protocol: UDP
      dnsPolicy: ClusterFirst
      restartPolicy: Always
"""
        default:
        return ''
    }
}

String getServiceYaml(service, name) {
    switch (service) {
        case 'mongodb':
        return """
apiVersion: v1
kind: Service
metadata:
  name: ${name}
  labels:
    name: ${name}
spec:
  ports:
  - port: 27017
    protocol: TCP
    targetPort: 27017
  selector:
    name: ${name}
  type: ClusterIP
"""
        case 'mysql':
        return """
apiVersion: v1
kind: Service
metadata:
  name: ${name}
  labels:
    name: ${name}
spec:
  ports:
  - port: 3306
    protocol: TCP
    targetPort: 3306
  selector:
    name: ${name}
  type: ClusterIP
"""
        case 'memcached':
            return """
apiVersion: v1
kind: Service
metadata:
  name: ${name}
  labels:
    name: ${name}
spec:
  ports:
  - port: 11211
    name: memcached-tcp
    protocol: TCP
  - port: 11211
    name: memcached-udp
    protocol: UDP
  selector:
    name: ${name}
  type: ClusterIP
"""
        default:
        return ''
    }
}
