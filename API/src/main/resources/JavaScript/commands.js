use = function (arg1) {
  return Commands.call("use", arg1);
};

wait = function (arg1, arg2, arg3) {
  return Commands.call("wait", arg1, arg2, arg3);
};

waitVanish = function (arg1, arg2, arg3) {
  return Commands.call("waitVanish", arg1, arg2, arg3);
};

exists = function (arg1, arg2, arg3) {
  return Commands.call("exists", arg1, arg2, arg3);
};

click = function (arg1, arg2, arg3, arg4, arg5, arg6) {
  return Commands.call("click", arg1, arg2, arg3, arg4, arg5, arg6);
};

doubleClick = function (arg1, arg2, arg3, arg4, arg5, arg6) {
  return Commands.call("doubleClick", arg1, arg2, arg3, arg4, arg5, arg6);
};

rightClick = function (arg1, arg2, arg3, arg4, arg5, arg6) {
  return Commands.call("rightClick", arg1, arg2, arg3, arg4, arg5, arg6);
};

hover = function (arg1, arg2, arg3, arg4, arg5, arg6) {
  return Commands.call("hover", arg1, arg2, arg3, arg4, arg5, arg6);
};

type = function(arg1, arg2, arg3, arg4, arg5, arg6) {
  return Commands.call("type", arg1, arg2, arg3, arg4, arg5, arg6);
};

paste = function(arg1, arg2, arg3, arg4, arg5, arg6) {
  return Commands.call("paste", arg1, arg2, arg3, arg4, arg5, arg6);
};
