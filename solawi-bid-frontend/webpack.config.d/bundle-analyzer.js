// Aktiviert den Bundle-Analyzer nur, wenn ANALYZE=1 gesetzt ist.
// Aufruf: ANALYZE=1 ./gradlew :solawi-bid-frontend:jsBrowserProductionWebpack
if (process.env.ANALYZE === '1') {
    const { BundleAnalyzerPlugin } = require('webpack-bundle-analyzer');
    config.plugins = config.plugins || [];
    config.plugins.push(new BundleAnalyzerPlugin({
        analyzerMode: 'static',
        openAnalyzer: false,
        reportFilename: 'bundle-report.html',
        defaultSizes: 'gzip'
    }));
}