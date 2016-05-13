import groovy.json.*

def pe_request( Map parameters = [:], String endpoint ) {
  assert parameters.port instanceof Integer
  assert parameters.endpoint instanceof String

  def curl_cmd = "curl --cacert /var/lib/jenkins/ca.pem https://${puppet_master}:${parameters.port}/${endpoint}"

  if (parameters.method) {
    assert parameters.method instanceof String
    assert ['GET','POST','DELETE','PUT'].contains(parameters.method)
  } else {
    parameters.method = 'GET'
  }
  curl_cmd = curl_cmd + " -X ${parameters.method} -H 'Content-Type: application/json "

  if (parameters.body) {
    assert parameters.body instanceof Map
    parameters.body = new JsonBuilder(parameters.body).toString()
    curl_cmd = curl_cmd + " -d ${parameters.body} "
  }

  def token = readFile('/var/lib/jenkins/.puppetlabs/token').trim()
  curl_cmd = curl_cmd + " -H 'X-Authentication: ${token}' "

  sh "cat /etc/puppetlabs/puppet/puppet.conf  | grep server | cut -d' ' -f3 > return_body"
  def puppet_master = readFile('return_body').trim()

  sh "${curl_cmd} > return_body"
  def return_body = readFile('return_body').trim()

  def result = json_parser(return_body)

  if (result.error) {
    throw new Exception("Error creating Puppet Enterprise job: ${result.error}")
  }

  return result
}

def json_parser(string) {
  def slurper = new JsonSlurper()
  return slurper.parseText(string)
}

def run( Map parameters = [:], String environment ) {
  def api = '/orchestrator/v1'

  def request_body = ['environment': environment, 'noop': false]

  if (parameters.target) {
    assert parameters.target instanceof String
    request_body.target = parameters.target
  }

  if (parameters.noop) {
    assert parameters.noop instanceof Boolean
    request_body.noop = parameters.noop
  }

  if (parameters.concurrency) {
    assert parameters.concurrency instanceof Integer
    request_body.concurrency = parameters.concurrency
  }

  return pe_request("${api}/command/deploy", port: 8143, method: 'POST', body: request_body)
}

def deployCode( environments = 'all' ) {
  def api = '/code-manager/v1'
  def request_body = [:]

  if (environment == 'all') {
    request_body['deploy-all'] = true
  } else {
    request_body['environments'] = environments
  }
    
  pe_request("${api}/deploys", port: 8170, method: 'POST', body: request_body)
}

return this;
