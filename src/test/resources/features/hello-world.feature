Feature: Demo basic usage of karate

  Scenario: make a get call to rest api and assert JSON Body
    Given url 'https://httpbin.org'
    And path 'get'
    And param msg = "Lets Test api with Karate"
    When method get
    Then status 200
    And match response.args == {"msg":"Lets Test api with Karate"}
    And match response.headers.Host == '#string'

  Scenario: Introduction of javascript and JSON as first class citizens
    * def getRequest =
    """
    function(title, name) {
      var randLetter = String.fromCharCode(65 + Math.floor(Math.random() * 26));
      var uniqueId = randLetter + Date.now();
      var person = {};
      person['id'] = uniqueId;
      person['title'] = title;
      person['name'] = name;
      return person
    }
    """
    Given url 'https://httpbin.org'
    And path 'post'
    And request getRequest('Mr.', 'Vijay')
    When method post
    Then status 200