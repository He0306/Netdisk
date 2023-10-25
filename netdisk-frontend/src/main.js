import {createApp} from 'vue'
import App from './App.vue'
import router from './router'

// 引入elementPlus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// 引入图标
import '@/assets/icon/iconfont.css'
import '@/assets/base.scss'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/theme-chalk/dark/css-vars.css'
import '@/assets/css-vars.css'
// 引入代码高亮
import HIjsVuePlugin from "@highlightjs/vue-plugin"
import "highlight.js/styles/atom-one-light.css"
import "highlight.js/lib/common"
// 引入cookie
import VueCookies from 'vue-cookies'

import Message from '@/utils/Message'
import Request from '@/utils/Request'
import Confirm from "@/utils/Confirm";
import Utils from "@/utils/Utils";

// 自定义组件
import Avatar from '@/components/Avatar.vue'
import Icon from '@/components/Icon.vue'
import NoData from "@/components/NoData.vue";
import Table from "@/components/Table.vue"
import FolderSelect from "@/components/FolderSelect.vue"
import Navigation from "@/components/Navigation.vue"
import Preview from "@/components/preview/Preview.vue"
import Window from "@/components/Window.vue"

const app = createApp(App)

app.component('Preview', Preview)
app.component('Window', Window)
app.component('FolderSelect', FolderSelect)
app.component('Navigation', Navigation)
app.component("Avatar", Avatar)
app.component("Icon", Icon)
app.component("NoData", NoData)
app.component("Table", Table)
app.use(router)
app.use(ElementPlus)
app.use(HIjsVuePlugin)
// 配置全局组件
app.config.globalProperties.Confirm = Confirm
app.config.globalProperties.Message = Message
app.config.globalProperties.Request = Request
app.config.globalProperties.Utils = Utils
app.config.globalProperties.VueCookies = VueCookies
app.config.globalProperties.globalInfo = {
    avatarUrl: "/api/getAvatar/",
    imageUrl: "/api/file/getImage/"
}
// 全局注册el-icon
for (const [name, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(name, component);
}
app.mount('#app')
