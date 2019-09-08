Feature: Demo basic usage of karate

  Scenario: make a get call to rest api and assert JSON Body
    Given url 'https://httpbin.org'
    And path 'get'
    And param msg = "Lets Test api with Karate"
    When method get
    Then status 200
    And match response.args == {"msg":"Lets Test api with Karate"}
    And match response.headers.Host == '#string'
