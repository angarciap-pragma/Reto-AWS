const users = new Map([
  ["1001", { id: "1001", nombre: "Andrea Garcia", email: "andrea@demo.com" }],
  ["1002", { id: "1002", nombre: "Pepito Perez", email: "pepito@demo.com" }]
]);

const json = (statusCode, body) => ({
  statusCode,
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify(body)
});

module.exports = {
  users,
  json
};

