# Validation report

Date: 2026-07-09

## Environment
```
openjdk version "21.0.10" 2026-01-20
OpenJDK Runtime Environment (build 21.0.10+7-Debian-1deb13u1)
OpenJDK 64-Bit Server VM (build 21.0.10+7-Debian-1deb13u1, mixed mode, sharing)

bash: line 10: mvn: command not found

bash: line 12: docker: command not found
```

## Static validations executed in this sandbox

### pom.xml parse
```
pom.xml XML parse OK
```

### Domain layer javac syntax compile with available JDK 21
```
Domain layer compile OK with JDK 21 syntax check
```


### Domain + application service syntax compile with Spring annotation stubs
```
Domain + application service syntax compile OK with JDK 21 and local Spring annotation stubs
```

### Full Maven verification attempt
```
bash: line 34: mvn: command not found
```

## Result
Full Maven verification could not be executed in this sandbox because Maven is not installed and outbound dependency downloads are not available. The project includes scripts/maven-with-docker.sh to run clean verify with Maven 3.9.11 + Eclipse Temurin 25 in a normal Docker-enabled environment.
