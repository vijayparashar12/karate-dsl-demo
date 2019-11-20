Feature: call workflow network to demo auth features

  Background:
    * configure headers = read('classpath:features/functions/token.js')

  Scenario: get process-types from wfn
    Given url workflow_network
    And path 'api/process-types'
    When method get
    Then status 200
    And match response.process_types == '#[] #string'