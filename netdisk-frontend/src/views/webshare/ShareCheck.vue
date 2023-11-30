<template>
    <div class="share">
        <div class="body-content">
            <div class="logo">
                <span class="iconfont icon-pan"></span>
                <span class="name">云盘</span>
            </div>
            <div class="code-panel">
                <div class="file-info">
                    <div class="avatar">
                        <Avatar
                                :userId="shareInfo.userId"
                                :avatar="shareInfo.avatar"
                                :width="50"></Avatar>
                    </div>
                    <div class="share-info">
                        <div class="user-info">
                            <span class="nick-name">{{ shareInfo.nickName }}</span>
                            <span class="share-time">分享于：{{ shareInfo.shareTime }}</span>
                        </div>
                        <div class="file-name">分享文件：{{ shareInfo.fileName }}</div>
                    </div>
                </div>
                <div class="code-body">
                    <div class="tips">请输入提取码：</div>
                    <div class="input-area">
                        <el-form :model="form" ref="formRef" :rules="formRules" @submit.prevent>
                            <el-form-item prop="code">
                                <el-input :minlength="5" :maxlength="5"
                                          class="input"
                                          clearable
                                          placeholder="请输入提取码"
                                          v-model.trim="form.code">
                                </el-input>
                                <el-button type="primary" @click="checkShare">提取文件</el-button>
                            </el-form-item>
                        </el-form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import Avatar from "@/components/Avatar.vue";
import {getCurrentInstance, ref} from "vue";
import {useRoute, useRouter} from "vue-router";

const router = useRouter()
const route = useRoute()
const {proxy} = getCurrentInstance()

const shareInfo = ref({})
const form = ref({})
const formRules = {
    code: [{required: true, message: "请输入提取码"}]
}
const formRef = ref()

const api = {
    getShareInfo: "/showShare/getShareInfo",
    checkShareCode: "/showShare/checkShareCode"
}
const shareId = route.params.shareId
const getShareInfo = async () => {
    let result = await proxy.Request({
        url: api.getShareInfo,
        params: {
            shareId
        }
    })
    if (!result) {
        proxy.Message.error(result.error)
        return
    }
    shareInfo.value = result.data
}
const checkShare = async () => {
    formRef.value.validate(async (valid) => {
        if (!valid) {
            return;
        }
        let params = {}
        Object.assign(params, form.value)
        let result = await proxy.Request({
            url: api.checkShareCode,
            params: {
                shareId: shareId,
                code: form.value.code
            }
        })
        if (!result){
            proxy.Message.error(result.message)
            return
        }
        router.push(`/share/${shareId}`)
    })
}
getShareInfo()
</script>

<style lang="scss" scoped>
.share {
  height: calc(100vh);
  background: url("../../assets/icon-image/share_bj.png");
  background-repeat: repeat-x;
  background-position: 0 bottom;
  background-color: #eef2f6;
  display: flex;
  justify-content: center;

  .body-content {
    margin-top: calc(100vh / 5);
    width: 500px;

    .logo {
      display: flex;
      align-items: center;
      justify-content: center;

      .icon-pan {
        font-size: 60px;
        color: #409eff;
      }

      .name {
        font-weight: bold;
        margin-left: 5px;
        font-size: 25px;
        color: #409eff;
      }
    }

    .code-panel {
      margin-top: 20px;
      background: #fff;
      border-radius: 5px;
      overflow: hidden;
      box-shadow: 0 0 7px 1px #5757574f;

      .file-info {
        padding: 10px 20px;
        background: #409eff;
        color: #ffffff;
        display: flex;
        align-items: center;

        .avatar {
          margin-right: 5px;
        }

        .share-info {
          .user-info {
            display: flex;
            align-items: center;

            .nick-name {
              font-size: 15px;
            }

            .share-time {
              margin-left: 20px;
              font-size: 12px;
            }
          }

          .file-name {
            margin-top: 10px;
            font-size: 12px;
          }
        }
      }

      .code-body {
        padding: 30px 20px 60px 20px;

        .tips {
          font-weight: bold;
        }

        .input-area {
          margin-top: 10px;

          .input {
            flex: 1;
            margin-right: 10px;
          }
        }
      }
    }
  }
}
</style>