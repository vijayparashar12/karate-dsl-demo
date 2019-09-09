function fn() {
  var token_file = karate.get("token_file")
  return { Authorization: 'Bearer ' + karate.read(token_file) };
}