<template>
    <div class="share">
        <div class="header">
            <div class="header-content">
                <div class="logo" @click="jump">
                    <span class="iconfont icon-pan"></span>
                    <span class="name">云盘</span>
                </div>
            </div>
        </div>
    </div>
    <div class="share-body">
        <template v-if="Object.keys(shareInfo).length === 0">
            <div class="loading" v-loading="Object.keys(shareInfo).length === 0"></div>
        </template>
        <template v-else>
            <div class="share-panel">
                <div class="share-user-info">
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
                        <div class="file-name">
                            分享文件：{{ shareInfo.fileName }}
                        </div>
                    </div>
                    <div class="share-op-btn">
                        <el-button
                                type="primary"
                                v-if="shareInfo.cureentUser"
                                @click="cancelShare">
                            <span class="iconfont icon-cancel"></span>
                            取消分享
                        </el-button>
                        <el-button
                                type="primary"
                                v-else
                                @click="save2MyPan"
                                :disabled="selectFileIdList.length === 0">
                            <span class="iconfont icon-import"></span>
                            保存到我的网盘
                        </el-button>
                    </div>
                </div>
            </div>
        </template>
    </div>
</template>

<script setup>
import {getCurrentInstance, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import Avatar from "@/components/Avatar.vue";

const {proxy} = getCurrentInstance()
const router = useRouter()
const route = useRoute()
const api = {
    getShareLoginInfo: "/showShare/getShareLoginInfo",
    loadFileList: "/showShare/loadFileList",
    createDownLoadUrl: "/showShare/createDownLoadUrl",
    download: "/api/showShare/download",
    cancelShare: "/share/cancelShare",
    saveShare: "/showShare/saveShare"
}
const shareId = route.params.shareId
const shareInfo = ref({})
const getShareInfo = async () => {
    let result = await proxy.Request({
        url: api.getShareLoginInfo,
        showLoading: false,
        params: {
            shareId
        }
    })
    if (!result) {
        proxy.Message.error(result.message)
        return
    }
    if (result.data == null) {
        router.push(`/shareCheck/${shareId}`)
        return
    }
    shareInfo.value = result.data
}
</script>

<style lang="scss" scoped>
@import "@/assets/file.list.scss";

.header {
  width: 100%;
  position: fixed;
  background: #0c95f7;
  height: 50px;

  .header-content {
    width: 70%;
    margin: 0px auto;
    color: #ffffff;
    line-height: 50px;

    .logo {
      display: flex;
      align-items: center;
      cursor: pointer;

      .icon-pan {
        font-size: 40px;
      }

      .name {
        font-weight: bold;
        margin-left: 5px;
        font-size: 25px;
      }
    }
  }
}

.share-body {
  width: 70%;
  margin: 0 auto;
  padding-top: 50px;

  .loading {
    height: calc(100vh / 2);
    width: 100%;
  }

  .share-panel {
    margin-top: 20px;
    display: flex;
    justify-content: space-around;
    border-bottom: 1px solid #dddddd;
    padding-bottom: 10px;

    .share-user-info {
      flex: 1;
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
  }
}

.file-list {
  margin-top: 10px;

  .file-item {
    .op {
      width: 170px;
    }
  }
}
</style>