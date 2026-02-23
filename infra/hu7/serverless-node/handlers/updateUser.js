const { users, json } = require("../data/store");

module.exports.handler = async (event) => {
  const id = event.pathParameters?.id;
  const body = JSON.parse(event.body || "{}");
  const { nombre, email } = body;

  if (!id) return json(400, { message: "id es obligatorio" });
  if (!nombre || !email) return json(400, { message: "nombre y email son obligatorios" });
  if (!users.has(id)) return json(404, { message: "Usuario no encontrado" });

  const updated = { id, nombre, email };
  users.set(id, updated);
  return json(200, updated);
};

