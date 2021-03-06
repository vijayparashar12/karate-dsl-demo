Feature: Demo basic usage of karate

  Background:
    * def uuid =
    """
    function(){ return java.util.UUID.randomUUID() + '' }
    """

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
    * def id = uuid()
    * def payload = getRequest(id,'Mr.', 'Vijay')
    * def postCall = call read('./post-request.feature') payload
    * match postCall.response.json.id == '#uuid'

  Scenario: read payload from fine
    * def payload = read('../json/request.json')
    * def postCall = call read('./post-request.feature') payload
    * match postCall.response.json.id == '#number'
    And match postCall.response.json == read('../json/request.json')

  Scenario: Showoff some more cool ways to call java
    * def dateStringToLong =
      """
      function(s) {
        var SimpleDateFormat = Java.type('java.text.SimpleDateFormat');
        var sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        return sdf.parse(s).time; // '.getTime()' would also have worked instead of '.time'
      }
      """
    * assert dateStringToLong("2016-12-24T03:39:21.081+0000") == 1482550761081