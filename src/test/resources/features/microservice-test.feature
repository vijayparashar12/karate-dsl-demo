Feature: demo micro service test by karate

  Background:
    * configure headers = read('classpath:features/functions/token.js')
    * def nakadi = read('classpath:features/functions/nakadi.js')

    * def wait =
    """
    function(pause){ java.lang.Thread.sleep(pause) }
    """

    * def replace =
    """
    function(str,find,replacement) {
      return str.replace(find, replacement)
    }
    """

  Scenario: verify process instance creation

    Given url process_instance_generator
    And path '/api/sites/moenchengladbach/processes/bagstore--bgs_dropout--bgs-dropout'
    And request read('../json/pi_create_request.json')
    When method post
    Then status 201
    And assert responseTime < 1500

    * def movement_pi = responseHeaders['Location'][0]

    Given url movementBaseURL
    And path movement_pi
    When method get
    Then status 200

    * eval wait(3000)

    * def pi_uri = replace(movement_pi, '/api','')
    * def whereClause = "e_event->>'process_instance' = '"+pi_uri +"'"

    * def events = nakadi("de.zalando.logistics.wolf.movements-dev.process_status_changed",whereClause)

    * assert events.events.length == 1
    * match events.events[0].status == 'CREATED'