const { defineConfig } = require('@vue/cli-service')
const path = require('path')

module.exports = defineConfig({
  configureWebpack: {
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src')
      }
    }
  },
  chainWebpack: config => {
    config.plugin('define').tap(args => {
      Object.assign(args[0], {
        __VUE_OPTIONS_API__: JSON.stringify(true),
        __VUE_PROD_DEVTOOLS__: JSON.stringify(false),
        __VUE_PROD_HYDRATION_MISMATCH_DETAILS__: JSON.stringify(false)
      })
      return args
    })
  },
  devServer: {
    port: 8085,
    proxy: {
      '/api': {
        target: 'http://localhost:8087',
        changeOrigin: true
      }
    }
  }
}) 