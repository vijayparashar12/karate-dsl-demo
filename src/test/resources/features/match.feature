Feature: various ways to assert payload with match keyword


  Scenario: json operations
    * def myJson = { foo: 'bar' }
    * set myJson.foo = 'world'
    * match myJson == { foo: 'world' }

  # add new keys.  you can use pure JsonPath expressions (notice how this is different from the above)
    * set myJson $.hey = 'ho'
    * match myJson == { foo: 'world', hey: 'ho' }

  # and even append to json arrays (or create them automatically)
    * set myJson.zee[0] = 5
    * match myJson == { foo: 'world', hey: 'ho', zee: [5] }

  # omit the array index to append
    * set myJson.zee[] = 6
    * match myJson == { foo: 'world', hey: 'ho', zee: [5, 6] }

  # nested json ? no problem
    * set myJson.cat = { name: 'Billie' }
    * match myJson == { foo: 'world', hey: 'ho', zee: [5, 6], cat: { name: 'Billie' } }

  # and for match - the order of keys does not matter
    * match myJson == { cat: { name: 'Billie' }, hey: 'ho', foo: 'world', zee: [5, 6] }

  # you can ignore fields marked with '#ignore'
    * match myJson == { cat: '#ignore', hey: 'ho', foo: 'world', zee: [5, 6] }

  # see more at https://github.com/intuit/karate#fuzzy-matching
  Scenario: Fuzzy Matching
    * def cat = { name: 'Billie', type: 'LOL', id: 'a9f7a56b-8d5c-455c-9d13-808461d17b91' }
    * match cat == { name: '#ignore', type: '#regex [A-Z]{3}', id: '#uuid' }

  Scenario: JsonPath Matching
    * def myJson = { foo: 'world', hey: 'ho', zee: [5, 6], cat: { name: 'Billie' } }
    * def x = karate.jsonPath(myJson,"$.cat.name")
    * match x == "Billie"

    * def cat =
      """
      {
        name: 'Billie',
        kittens: [
          { id: 23, name: 'Bob' },
          { id: 42, name: 'Wild' }
        ]
      }
      """

    * def bob = get[0] cat.kittens[?(@.id==23)]
    * match bob.name == 'Bob'

    * def temp = karate.jsonPath(cat, "$.kittens[?(@.name=='" + bob.name + "')]")
    * match temp[0] == bob