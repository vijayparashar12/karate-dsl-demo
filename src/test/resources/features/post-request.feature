Feature: Demo reusable features

  Scenario: make POST call
    Given url httpBin
    And path 'post'
    And request __arg
    When method post
    Then status 200
    And match response.json.id == '#notnull'
    And match response.json.name == 'Vijay'