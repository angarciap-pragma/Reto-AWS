const { users, json } = require("../data/store");

module.exports.handler = async (event) => {
  const id = event.pathParameters?.id;
  if (!id) return json(400, { message: "id es obligatorio" });

  const user = users.get(id);
  if (!user) return json(404, { message: "Usuario no encontrado" });

  return json(200, user);
};

