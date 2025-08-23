const devPath = require("path");

config.devServer = {
  ...config.devServer, // Merge with other devServer settings

  headers: {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, PATCH, OPTIONS",
      "Access-Control-Allow-Headers": "X-Requested-With, content-type, Authorization"
  },

   // Serve files like /favicon.ico, /img/*, /css/* directly from public/
   static: [
       {
         directory: devPath.resolve(__dirname, "../solawi-bid-solawi-bid-frontend/kotlin"),
         publicPath: "/"
       },
       {
         directory: devPath.resolve(__dirname, "../../../../solawi-bid-frontend/build/processedResources/js/main"),
         publicPath: "/"
       }
     ],

   // SPA routing fallback (critical!)
   historyApiFallback: {
     rewrites: [
       {
         from: /^(?!\/(img|css|assets|i18n|solawi-bid-frontend\.js)).*$/,
         to: '/index.html'
       }
     ],
     disableDotRule: true  // don't misinterpret UUID/dash routes as files
   },
   hot: true,
   compress: true,
};
