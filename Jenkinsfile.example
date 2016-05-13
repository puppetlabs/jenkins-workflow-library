// This is an example Jenkinsfile

node {
  puppet = load 'puppet_enterprise.groovy'

  stage 'Deploy Dev'
  puppet.deployCode 'dev'
  puppet.run 'dev'

  stage 'Deploy Staging Dry Run'
  sh 'git checkout staging'
  sh 'git merge dev'
  sh 'git push origin staging'
  puppet.deployCode 'staging'
  puppet.run 'staging', noop: true

  stage 'Production Promotion'
  input 'Ready to promote?'
  puppet.run 'staging'

  stage 'Deploy Rgbank to Production'
  sh 'git checkout production'
  sh 'git merge staging'
  sh 'git push origin production'
  puppet.deployCode 'production'
  puppet.run 'production', target: 'Rgbank'

  stage 'Deploy All of Production'
  puppet.run 'production'
}
