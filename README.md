[![Build Status](https://travis-ci.org/mborges-pivotal/tPCF-ers-demo1.svg?branch=master)](https://travis-ci.org/mborges-pivotal/tPCF-ers-demo1)
[ ![Download](https://api.bintray.com/packages/mborges-pivotal/generic/tPCF-ers-demo1/images/download.svg) ](https://bintray.com/mborges-pivotal/generic/tPCF-ers-demo1/_latestVersion)

# tPCF Elastic Runtime Service (ERS) Base Demo
Base application to demonstrate tPCF ERS

## Credits and contributions
As you all know, we often transform other work into our own. This is all based from Andrew Ripka's [cf-workshop-spring-boot github repo](https://github.com/pivotal-cf-workshop/cf-workshop-spring-boot) with some basic modifications.

## Introduction
This base application is intended to demonstrate some of the basic functionality of tPCF ERS:

* tPCF api, target, login, and push
* tPCF environment variables
  * Spring Cloud Profiles
* Scaling, self-healing, router and load balancing
* RDBMS service and application auto-configuration
* Blue green deployments

## Getting Started

**Prerequisites**
- [Cloud Foundry CLI](https://github.com/cloudfoundry/cli)
- [Git Client](https://git-scm.com/downloads)
- An IDE (IntelliJ, Eclispe, VSCode etc.)
- JDK 21

**Building**
```
$ git clone [REPO]
$ cd [REPO]
$ ./mvnw clean install
``` 

### To run the application locally
The application is set to use an embedded H2 database in non-PaaS environments, and to take advantage of Tanzu Platform for Cloud Foundry's auto-configuration for services. To use a Postgres Dev service in Tanzu Platform for Cloud Foundry, simply create and bind a service to the app and restart the app. No additional configuration is necessary when running locally or in Tanzu Platform for Cloud Foundry.

In Tanzu Platform for Cloud Foundry, it is assumed that a the Postgres service tile will be used.

```
$ ./mvnw spring-boot:run
```

Then go to the http://localhost:8080 in your browser

### Running on Cloud Foundry
Take a look at the manifest file for the recommended setting. Adjust them as per your environment.


