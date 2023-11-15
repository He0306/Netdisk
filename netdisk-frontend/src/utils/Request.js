import axios from "axios";
import {ElLoading} from "element-plus";
import Message from "./Message";
import router from "../router";

const contentTypeForm = 'application/x-www-form-urlencoded;charset=UTF-8'
const contentTypeJson = 'application/json'

const responseTypeJson = "json"

let loading = null
const instance = axios.create({
    baseURL: '/api',
    timeout: 10 * 1000
})

// 请求拦截器
instance.interceptors.request.use(
    (config) => {
        if (config.showLoading) {
            loading = ElLoading.service({
                lock: true,
                text: "加载中......",
                background: 'rgba(0,0,0,0.7)'
            })
        }
        return config;
    },
    (error) => {
        if (config.showLoading && loading) {
            loading.close()
        }
        Message.error("请求发送失败！")
        return Promise.reject("请求发送失败！")
    }
)

// 响应拦截器
instance.interceptors.response.use(
    (response) => {
        const {showLoading, errorCallback, showError = true, responseType} = response.config
        if (showLoading && loading) {
            loading.close()
        }
        const responseData = response.data
        
        if (responseType === "arraybuffer" || responseType === "blob") {
            return responseData;
        }
        // 正常请求
        if (responseData.status === 200) {
            return responseData
        } else if (responseData.status === 901) {
            // 登录超时
            router.push("/login?redirectUrl=" + encodeURI(router.currentRoute.value.path));
            return Promise.reject({showError: false, message: "登录超时，请重新登录！"})
        } else {
            // 其他错误
            if (errorCallback) {
                errorCallback(responseData.message)
            }
            return Promise.reject({showError: showError, message: responseData.message})
        }
    },
    (error) => {
        if (error.config.showLoading && loading) {
            loading.close()
        }
        return Promise.reject({showError: true, message: "系统异常，请联系管理员！"})
    }
)

const request = (config) => {
    const {url, params, dataType, showLoading = true, responseType = responseTypeJson} = config
    let contentType = contentTypeForm
    let formData = new FormData()
    for (let key in params) {
        formData.append(key, params[key] === undefined ? "" : params[key])
    }
    if (dataType !== null && dataType === 'json') {
        contentType = contentTypeJson
    }
    let headers = {
        'Content-Type': contentType,
        'X-Requested-With': 'XMLHttpRequest'
    }
    return instance.post(url, formData, {
        onUploadProgress: (event) => {
            if (config.uploadProgressCallback) {
                config.uploadProgressCallback(event)
            }
        },
        responseType: responseType,
        headers: headers,
        showLoading: showLoading,
        errorLoading: config.errorCallback,
        showError: config.showError
    }).catch(error => {
        console.log(error)
        if (!error.showError) {
            Message.error(error.message)
        }
        Message.error(error.message)
    })
}
export default request
