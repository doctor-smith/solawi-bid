// Let Webpack render the index.html and automatically inject the (possibly hashed)
// bundle as <script>. This way the bundle name never needs to be manually
// maintained in the HTML and cache-busting (see cache-busting.js) works
// automatically.
const HtmlWebpackPlugin = require('html-webpack-plugin');
const path = require('path');

// Kotlin/JS inlines webpack.config.d/*.js contents into
// build/js/packages/<module>/webpack.config.js. So __dirname at runtime is
// build/js/packages/<module>/ — four levels below the project root.
const projectDir = path.resolve(__dirname, '..', '..', '..', '..', 'solawi-bid-frontend');
const template = path.resolve(projectDir, 'src', 'main', 'resources', 'index.html');
// debug
// console.log('[html.js] resolved template path:', template);

// Make cache-busting effective: nginx / Dev-Server should never cache index.html,
// but Webpack puts the hash name of the bundle directly into the HTML.
config.plugins = config.plugins || [];
config.plugins.push(new HtmlWebpackPlugin({
    template: template,
    inject: 'body',
    scriptLoading: 'defer',
    // Important: no minification in dev, standard-minify is fine in prod
    minify: config.mode === 'production'
}));

// Ensure real errors from webpack are visible (Kotlin/JS default suppresses them).
config.stats = config.stats || {};
config.stats.errors = true;
config.stats.errorDetails = true;
config.stats.warnings = true;
