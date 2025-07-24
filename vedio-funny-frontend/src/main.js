import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 导入路由权限控制
import '@/router/permission'

import './assets/styles/global.css' // 引入全局样式文件

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
  // 同时注册为小写形式，以防万一
  app.component(key.toLowerCase(), component)
}



app.use(store)
app.use(router)
app.use(ElementPlus)

app.mount('#app') 