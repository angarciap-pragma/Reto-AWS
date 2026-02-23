const { users, json } = require("../data/store");

module.exports.handler = async (event) => {
  const id = event.pathParameters?.id;
  if (!id) return json(400, { message: "id es obligatorio" });
  if (!users.has(id)) return json(404, { message: "Usuario no encontrado" });

  users.delete(id);
  return json(200, { message: "Usuario eliminado", id });
};

