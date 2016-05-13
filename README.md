# jenkins-workflow-library

This project provides a library that makes it easier to interface with Puppet
Enterprise services in a project's Jenkinsfile.

## Setup

You'll need to generate a [Puppet Enterprise RBAC
token](https://docs.puppet.com/pe/latest/rbac_token_auth.html) that has
permission to use the orchestrator and deploy code.  If you install the
[pe-client-tools](https://docs.puppet.com/pe/latest/install_pe_client_tools.html)
on the Jenkins master, you can generate the token with the following command
run as the `jenkins` user: `puppet access login --service-url $(puppet config
print master --config /etc/puppetlabs/puppet/puppet.conf)`

The token MUST be saved in ~jenkins/.puppetlabs/token (the command above saves
it at that location)

Finally, you'll need to copy the Puppet Master's CA certificate to
`/var/lib/jenkins/ca.pem` This command will do that for you run as root: `cp
/etc/puppetlabs/puppet/ssl/certs/ca.pem /var/lib/jenkins/ca.pem`


## Using

Place the `puppet_enterprise.groovy` file in the same directory as your
Jenkinsfile

In your Jenkinsfile, load the library and save the result to a variable. For
example:

```
puppet = load 'puppet_enterprise.groovy'
```

### Methods

#### deployCode

The deployCode method uses Puppet Enterprise's code management to deploy Puppet
code to the requested environment.

For example:

```
puppet.deployCode 'production'
```

Multiple environments can be specified with an array:
```
puppet.deployCode(['qa','staging'])
```

#### run

The run method uses Puppet Enterprise's application orchestration to run Puppet
across an entire environment, or a subset of.

**Options**

* target - Target an application, instance, or an application component in an environment. Defaults to the entire environment
* noop   - Perform a noop run. Defaults to false
* concurrency - Set the maximum concurrency.  Defaults to unlimited.

For example:

```
puppet.run 'production'
```

```
puppet.run 'production', target: 'Rgbank', concurrency: 10, noop: true
```

## Roadmap

* Turn into an actual Jenkins plugin.  Will provide config settings for RBAC token and Puppet Enterprise master URL
* Automatically pull CA certificate from Puppet Master
* Add ability to manage Node Manager functions such as modifying classes, parameters, and rules
