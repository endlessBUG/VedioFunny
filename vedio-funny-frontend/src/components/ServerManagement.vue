
<template>
  <div>
    <!-- ... existing code ... -->
    
    <!-- 分页组件 -->
    <a-pagination
      :total="total"
      :defaultPageSize="pageSize"
      :current="currentPage"
      @change="handlePageChange"
      showSizeChanger
      :pageSizeOptions="['20']"
      :showTotal="total => `Total ${total}`"
    >
      <!-- 添加跳转输入框 -->
      <template #buildOptionText="props">
        {{ props.value }} /page
      </template>
    </a-pagination>

    <!-- 添加跳转输入框 -->
    <div style="margin-top: 16px;">
      Go to <a-input-number v-model="jumpPage" :min="1" :max="Math.ceil(total / pageSize)" @pressEnter="handleJumpPage" />
    </div>

  </div>
</template>

<script>
import { defineComponent } from 'vue';
import { Pagination, InputNumber } from 'ant-design-vue';

export default defineComponent({
  components: {
    APagination: Pagination,
    AInputNumber: InputNumber,
  },
  data() {
    return {
      total: 2, // 总条数
      pageSize: 20, // 每页显示数量
      currentPage: 1, // 当前页码
      jumpPage: 1, // 跳转页码
    };
  },
  methods: {
    handlePageChange(page, pageSize) {
      this.currentPage = page;
      this.pageSize = pageSize;
      // 这里可以添加获取数据的逻辑
      console.log('Page changed to:', page);
      console.log('Page size changed to:', pageSize);
    },
    handleJumpPage() {
      if (this.jumpPage >= 1 && this.jumpPage <= Math.ceil(this.total / this.pageSize)) {
        this.currentPage = this.jumpPage;
        this.handlePageChange(this.currentPage, this.pageSize);
      }
    },
  },
});
</script>

<style scoped>
/* 添加样式 */
</style>
