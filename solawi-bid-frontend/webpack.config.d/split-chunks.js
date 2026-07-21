if (config.mode === 'production') {
    config.optimization = config.optimization || {};
    config.optimization.runtimeChunk = 'single';
    config.optimization.splitChunks = {
        chunks: 'all',
        maxInitialRequests: 20,
        maxAsyncRequests: 30,
        minSize: 20 * 1024,      // 20 KB
        cacheGroups: {
            kotlinRuntime: {
                test: /[\\/]node_modules[\\/](kotlin|kotlinx-.*|@js-joda)[\\/]/,
                name: 'kotlin-runtime',
                priority: 40,
                reuseExistingChunk: true
            },
            ktor: {
                test: /[\\/]node_modules[\\/](ktor-.*|io\.ktor.*)[\\/]/,
                name: 'ktor',
                priority: 35,
                reuseExistingChunk: true
            },
            compose: {
                test: /[\\/]node_modules[\\/](.*compose.*|.*jetbrains-compose.*)[\\/]/,
                name: 'compose',
                priority: 30,
                reuseExistingChunk: true
            },
            letsPlot: {
                test: /[\\/]node_modules[\\/](.*lets-plot.*)[\\/]/,
                name: 'lets-plot',
                priority: 25,
                reuseExistingChunk: true
            },
            vendor: {
                test: /[\\/]node_modules[\\/]/,
                name: 'vendor',
                priority: 10,
                reuseExistingChunk: true
            }
        }
    };
}