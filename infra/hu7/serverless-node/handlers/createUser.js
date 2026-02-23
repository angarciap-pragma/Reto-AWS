const { users, json } = require("../data/store");

module.exports.handler = async (event) => {
  const body = JSON.parse(event.body || "{}");
  const { id, nombre, email } = body;

  if (!id || !nombre || !email) {
    return json(400, { message: "id, nombre y email son obligatorios" });
  }
  if (users.has(id)) {
    return json(409, { message: "Usuario ya existe" });
  }

  const user = { id, nombre, email };
  users.set(id, user);
  return json(201, user);
};

