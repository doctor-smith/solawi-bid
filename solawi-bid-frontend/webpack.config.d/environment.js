/*
var webpack = require("webpack");
const path = require('path');
var dotenv = require('dotenv').config({ path: path.resolve(__dirname, '../../../../solawi-bid-frontend/.env') });
var definePlugin = new webpack.DefinePlugin(
   {
      "PROCESS_ENV": JSON.stringify(dotenv.parsed)
   }
);
config.plugins.push(definePlugin);
*/

// Imports at the top
const envPath = require("path");
const envWebpack = require("webpack");
const envDotenv = require("dotenv");

module.exports = (config) => {
  // Load .env file
  const env = envDotenv.config({
    path: envPath.resolve(__dirname, "../../../../solawi-bid-frontend/.env")
  });

  // Add DefinePlugin to inject env variables
  const definePlugin = new envWebpack.DefinePlugin({
    PROCESS_ENV: JSON.stringify(env.parsed)
  });

  // Ensure plugins array exists
  config.plugins = config.plugins || [];
  config.plugins.push(definePlugin);

  return config;
};
