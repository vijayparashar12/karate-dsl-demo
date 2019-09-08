Feature: Demo basic usage of karate

  Background:
  * def getRequest =  read("./functions/buildRequest.js")

  Scenario: make a get call to rest api and assert JSON Body
    Given url httpBin
    And path 'get'
    And param msg = "Lets Test api with Karate"
    When method get
    Then status 200
    And match response.args == {"msg":"Lets Test api with Karate"}
    And match response.headers.Host == '#string'

  Scenario: Introduction of javascript and JSON as first class citizens
    Given url httpBin
    And path 'post'
    And request getRequest('Mr.', 'Vijay')
    When method post
    Then status 200