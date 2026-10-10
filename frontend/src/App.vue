<template>
  <div class="dj-root" :class="{ 'big-screen-active': isBigScreenMode }">
    <!-- ========================================================================= -->
    <!-- 0. 登录界面 (未登录状态展示)                                             -->
    <!-- ========================================================================= -->
    <LoginView v-if="!isLoggedIn" @login-success="handleLoginSuccess" />

    <!-- ========================================================================= -->
    <!-- 1. 登录后普通业务端界面                                                    -->
    <!-- ========================================================================= -->
    <div v-else-if="!isBigScreenMode" class="dj-app">
      <!-- 顶部系统栏 (单层纯粹设计：庄严党建红金渐变，专注标识与用户控制) -->
      <header class="dj-header">
        <div class="header-left">
          <div class="logo-badge">
            <PartyEmblem style="width: 32px; height: 32px;" />
          </div>
          <div class="title-group">
            <h1>红河数据产业集团 · 智慧党建数字化平台</h1>
            <span class="sub-title">中共红河数据产业集团有限公司总支部委员会</span>
          </div>
        </div>

        <div class="header-right">
          <!-- 切换大屏按钮 -->
          <el-button 
            type="warning" 
            effect="dark" 
            icon="DataBoard" 
            class="big-screen-btn"
            @click="isBigScreenMode = true"
          >
            进入党建指挥大屏
          </el-button>

          <!-- 当前认证党务身份（只读标签，严格禁止任意下拉切换越权） -->
          <div class="current-auth-role-tag">
            <span class="role-static-label">党务权责身份：</span>
            <el-tag size="small" :type="currentRoleTagType" effect="dark" class="role-badge-static">
              <el-icon><Avatar /></el-icon> {{ currentRoleDisplayName }}
            </el-tag>
          </div>

          <!-- 用户名与组织标签 -->
          <div class="user-profile-badge">
            <el-avatar :size="26" class="user-avatar-small">{{ currentUser?.realName?.slice(0, 1) || '党' }}</el-avatar>
            <div class="user-name-dept-box">
              <span class="user-realname">{{ currentUser?.realName || '党员干部' }}</span>
              <span class="user-workno">({{ currentUser?.workNo || 'HH-000' }})</span>
            </div>
          </div>

          <el-tag type="danger" effect="plain" round class="org-tag">
            <el-icon><OfficeBuilding /></el-icon> {{ currentUserOrgShort }}
          </el-tag>

          <!-- 退出登录按钮 -->
          <el-button link type="info" icon="SwitchButton" class="logout-btn" @click="handleLogout">
            退出登录
          </el-button>
        </div>
      </header>

      <!-- 跑马灯合规警报条（平滑左移、悬停暂停、支持点击直达通知详情） -->
      <div class="compliance-marquee">
        <div class="marquee-tag">
          <el-icon><BellFilled /></el-icon>
          <span>党务合规雷达</span>
        </div>
        <div class="marquee-track-container">
          <div class="marquee-scroller">
            <!-- 渲染两组数据实现从右往左首尾无缝滚动动画 -->
            <div 
              v-for="(item, idx) in radarAlertsDoubled" 
              :key="idx" 
              class="alert-item" 
              :class="item.alertClass"
              @click="handleRadarAlertClick(item)"
              :title="`点击查看《${item.title}》通知详情并直达通知中心`"
            >
              <el-tag size="small" :type="item.tagType" effect="dark">{{ item.badgeText }}</el-tag>
              <span class="alert-text">{{ item.summary }}</span>
              <span class="click-hint">点击直达详情 »</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 主体内容区域 -->
      <main class="dj-main-container">
        <!-- 独立现代白底主导航卡片栏：与顶部栏彻底解耦、留出舒适呼吸间距，左右宽敞舒展不拥挤 -->
        <div class="dj-nav-deck">
          <div 
            v-for="item in navMenuItems" 
            :key="item.name"
            v-show="item.visible"
            class="deck-nav-item"
            :class="{ 'is-active': activeTab === item.name }"
            @click="activeTab = item.name"
          >
            <el-icon class="deck-item-icon"><component :is="item.icon" /></el-icon>
            <span class="deck-item-label">{{ item.label }}</span>
            <el-badge 
              v-if="item.name === 'notices' && unreadNoticeCount > 0" 
              :value="unreadNoticeCount" 
              class="deck-item-badge" 
            />
          </div>
        </div>

        <el-tabs v-model="activeTab" class="dj-content-tabs">
          <!-- 标签页 1: 发展党员全景工作台 (普通党员/支部书记/总支/纪检均可查阅，按权限控制操作) -->
          <el-tab-pane v-if="hasPermission('workbench:view')" name="workbench">
            <template #label>
              <span class="tab-label"><el-icon><Operation /></el-icon> 发展党员全景工作台</span>
            </template>

            <!-- 5 大阶段快捷流转横幅 -->
            <div class="stages-overview-card">
              <div class="card-header-row">
                <h3><el-icon><TrendCharts /></el-icon> 发展党员 5 大阶段全流程流转航标</h3>
                <span class="rule-hint">《中国共产党发展党员工作细则》严格规定之 25 个环扣步骤</span>
              </div>
              <div class="stages-stepper">
                <div 
                  v-for="stage in STAGES_AND_STEPS" 
                  :key="stage.stageId"
                  class="stage-step-item"
                  :class="{ 
                    'active-stage': selectedFilterStage === stage.stageId,
                    'has-members': getStageMemberCount(stage.stageId) > 0 
                  }"
                  @click="filterByStage(stage.stageId)"
                >
                  <div class="stage-index-num">0{{ stage.stageId }}</div>
                  <div class="stage-info">
                    <div class="stage-title">{{ stage.stageName }}</div>
                    <div class="stage-meta">
                      <span>{{ stage.steps.length }} 个标准化步骤</span>
                      <el-badge :value="getStageMemberCount(stage.stageId)" class="member-count-badge" type="danger" />
                    </div>
                  </div>
                  <div class="stage-arrow"><el-icon><ArrowRight /></el-icon></div>
                </div>
              </div>
            </div>

            <!-- 发展成员台账列表与过滤区 -->
            <div class="members-table-card">
              <div class="table-toolbar">
                <div class="toolbar-left">
                  <el-input 
                    v-model="searchKeyword" 
                    placeholder="搜索发展成员姓名、工号、岗位..." 
                    prefix-icon="Search"
                    clearable
                    style="width: 250px"
                  />
                  <el-select v-model="filterBranch" placeholder="按子公司党支部筛选" clearable style="width: 250px">
                    <el-option label="全部所属党组织" value="" />
                    <el-option label="中共红河红数信息技术服务有限公司支部委员会" value="中共红河红数信息技术服务有限公司支部委员会" />
                    <el-option label="中共云南幂次科技有限公司支部委员会" value="中共云南幂次科技有限公司支部委员会" />
                    <el-option label="中共红河链达科技有限公司支部委员会" value="中共红河链达科技有限公司支部委员会" />
                    <el-option label="中共红河数据产业集团有限公司总支部委员会" value="中共红河数据产业集团有限公司总支部委员会" />
                  </el-select>
                  <el-select v-model="filterSpecialType" placeholder="国企骨干属性" clearable style="width: 160px">
                    <el-option label="全部骨干属性" value="" />
                    <el-option label="生产经营一线" value="frontline" />
                    <el-option label="高知/技术研发" value="technical" />
                    <el-option label="“双培养”骨干" value="dual" />
                  </el-select>
                  <el-button v-if="selectedFilterStage || filterBranch || filterSpecialType || searchKeyword" link type="primary" @click="resetFilters">
                    重置筛选
                  </el-button>
                </div>

                <div class="toolbar-right">
                  <el-button v-if="hasPermission('workbench:create_applicant')" type="primary" icon="Plus" @click="openAddDialog">入党申请人建档</el-button>
                  <el-button v-if="hasPermission('workbench:export')" icon="Download" @click="exportTableData">导出发展党员合规台账</el-button>
                </div>
              </div>

              <!-- 数据表格 -->
              <el-table :data="filteredMembers" style="width: 100%" stripe class="custom-dj-table">
                <el-table-column label="发展成员" min-width="170">
                  <template #default="{ row }">
                    <div class="member-cell">
                      <el-avatar :size="36" class="avatar-red">{{ row.name.slice(0, 1) }}</el-avatar>
                      <div class="member-meta">
                        <div class="name-line">
                          <strong>{{ row.name }}</strong>
                          <el-tag size="small" effect="plain" class="workno-tag">{{ row.workNo }}</el-tag>
                        </div>
                        <span class="sub-job">{{ row.jobTitle }}</span>
                      </div>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="所属党支部及部门" min-width="240">
                  <template #default="{ row }">
                    <div class="branch-cell">
                      <span class="branch-title">
                        {{ row.branchName }}
                        <el-tag v-if="isCrossCompany(row.branchName, row.deptName)" size="small" type="warning" effect="plain" style="margin-left: 6px">跨单位挂靠/派驻</el-tag>
                      </span>
                      <span class="dept-title">
                        <span v-if="isCrossCompany(row.branchName, row.deptName)" style="color: #e6a23c; font-weight: 500">[人事单位] </span>
                        {{ row.deptName }}
                      </span>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="国企骨干标签" width="180">
                  <template #default="{ row }">
                    <div class="tags-wrapper">
                      <el-tag v-if="row.isFrontline" size="small" type="success">数据一线</el-tag>
                      <el-tag v-if="row.isTechnicalTalent" size="small" type="primary">科研骨干</el-tag>
                      <el-tag v-if="row.isDualCultivate" size="small" type="warning" effect="dark">双培养</el-tag>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="当前阶段与步骤" min-width="220">
                  <template #default="{ row }">
                    <div class="step-cell">
                      <el-tag :type="getStageTagType(row.currentStageId)" effect="light" class="stage-tag">
                        第 {{ row.currentStageId }} 阶段
                      </el-tag>
                      <div class="step-name">
                        <strong>第 {{ row.currentStepId }} 步：{{ getStepName(row.currentStepId) }}</strong>
                      </div>
                      <span class="stay-days">本步骤已停留 {{ row.daysInCurrentStep }} 天</span>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="党务合规状态" min-width="190">
                  <template #default="{ row }">
                    <div class="compliance-cell">
                      <el-tooltip :content="row.complianceAlert ? row.complianceAlert.message : '状态正常'" placement="top">
                        <el-tag :type="row.complianceAlert ? row.complianceAlert.type : 'success'" effect="plain" class="status-tag">
                          <el-icon v-if="row.complianceAlert && row.complianceAlert.type === 'danger'"><CircleCloseFilled /></el-icon>
                          <el-icon v-else-if="row.complianceAlert && row.complianceAlert.type === 'warning'"><WarningFilled /></el-icon>
                          <el-icon v-else><CircleCheckFilled /></el-icon>
                          {{ getComplianceShortText(row.complianceAlert) }}
                        </el-tag>
                      </el-tooltip>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="操作" width="220" fixed="right">
                  <template #default="{ row }">
                    <el-button type="primary" size="small" icon="Document" @click="openMemberDrawer(row)">
                      全景档案
                    </el-button>
                    <el-button 
                      v-if="hasPermission('workbench:advance')" 
                      size="small" 
                      icon="Right" 
                      @click="quickProgressStep(row)"
                    >
                      办理流转
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>

          <!-- 标签页 2: 所有党员花名册 (组织员/支部书记查阅维护) -->
          <el-tab-pane v-if="hasPermission('roster:view')" name="roster">
            <template #label>
              <span class="tab-label"><el-icon><User /></el-icon> 党员花名册</span>
            </template>

            <div class="roster-container-card">
              <!-- 花名册统计头部 -->
              <div class="roster-stats-banner">
                <div class="stat-pill">
                  <span class="pill-label">在册党员总数</span>
                  <span class="pill-val">{{ rosterList.length }} 人</span>
                </div>
                <div class="stat-pill">
                  <span class="pill-label">正式党员</span>
                  <span class="pill-val color-red">{{ getRosterCountByStatus(1) }} 人</span>
                </div>
                <div class="stat-pill">
                  <span class="pill-label">预备党员</span>
                  <span class="pill-val color-orange">{{ getRosterCountByStatus(2) }} 人</span>
                </div>
                <div class="stat-pill">
                  <span class="pill-label">发展对象/积极分子</span>
                  <span class="pill-val color-blue">{{ getRosterCountByStatus(3) + getRosterCountByStatus(4) }} 人</span>
                </div>
                <div class="stat-pill">
                  <span class="pill-label">入党申请人</span>
                  <span class="pill-val color-orange">{{ getRosterCountByStatus(5) }} 人</span>
                </div>
                <div class="stat-pill">
                  <span class="pill-label">一线与研发骨干率</span>
                  <span class="pill-val color-green">85.7%</span>
                </div>
                <div class="stat-pill">
                  <span class="pill-label">党费缴纳正常率</span>
                  <span class="pill-val color-green">100%</span>
                </div>
              </div>

              <!-- 搜索与筛选工具栏 -->
              <div class="table-toolbar">
                <div class="toolbar-left">
                  <el-input 
                    v-model="rosterSearchKeyword" 
                    placeholder="搜索姓名、工号、党内职务或部门..." 
                    prefix-icon="Search"
                    clearable
                    style="width: 250px"
                  />
                  <el-select v-model="rosterBranchFilter" placeholder="按子公司党支部" clearable style="width: 240px">
                    <el-option label="全部所属党组织" value="" />
                    <el-option label="红数信息支部" value="中共红河红数信息技术服务有限公司支部委员会" />
                    <el-option label="幂次科技支部" value="中共云南幂次科技有限公司支部委员会" />
                    <el-option label="链达科技支部" value="中共红河链达科技有限公司支部委员会" />
                    <el-option label="集团党总支" value="中共红河数据产业集团有限公司总支部委员会" />
                  </el-select>
                  <el-select v-model="rosterStatusFilter" placeholder="政治面貌/状态" clearable style="width: 150px">
                    <el-option label="全部政治面貌" value="" />
                    <el-option label="正式党员" :value="1" />
                    <el-option label="预备党员" :value="2" />
                    <el-option label="发展对象" :value="3" />
                    <el-option label="入党积极分子" :value="4" />
                    <el-option label="入党申请人" :value="5" />
                  </el-select>
                  <el-button v-if="rosterSearchKeyword || rosterBranchFilter || rosterStatusFilter" link type="primary" @click="resetRosterFilters">
                    重置
                  </el-button>
                </div>
                <div class="toolbar-right">
                  <el-button v-if="hasPermission('roster:create')" type="primary" icon="Plus" @click="openAddMemberDialog">新增党员</el-button>
                  <el-button v-if="hasPermission('roster:import')" type="warning" icon="Upload" @click="openImportRosterDialog">批量导入花名册</el-button>
                  <el-button v-if="hasPermission('roster:export')" icon="Download" @click="exportRosterExcel">导出花名册</el-button>
                </div>
              </div>

              <!-- 花名册数据表格 -->
              <el-table :data="filteredRosterList" style="width: 100%" stripe class="custom-dj-table">
                <el-table-column label="姓名" min-width="140">
                  <template #default="{ row }">
                    <div class="roster-name-cell">
                      <el-avatar :size="32" class="avatar-red">{{ row.name.slice(0, 1) }}</el-avatar>
                      <div>
                        <strong>{{ row.name }}</strong>
                        <div class="sub-workno">{{ row.workNo }}</div>
                      </div>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="性别/年龄" width="100">
                  <template #default="{ row }">
                    <span>{{ row.gender }} / {{ row.age }}岁</span>
                  </template>
                </el-table-column>

                <el-table-column label="政治面貌" width="130">
                  <template #default="{ row }">
                    <el-tag :type="getRosterPartyStatusTag(row.partyStatus)" effect="dark">
                      {{ getRosterPartyStatusText(row.partyStatus) }}
                    </el-tag>
                  </template>
                </el-table-column>

                <el-table-column label="党内职务" width="140">
                  <template #default="{ row }">
                    <el-tag size="small" type="info" effect="plain">{{ row.partyPost }}</el-tag>
                  </template>
                </el-table-column>

                <el-table-column label="党龄" width="100">
                  <template #default="{ row }">
                    <strong v-if="row.partyStatus === 1" class="standing-text">{{ calculatePartyStandingYears(row.joinPartyDate) }} 年</strong>
                    <span v-else class="text-muted">考察中</span>
                  </template>
                </el-table-column>

                <el-table-column label="入党/转正时间" width="150">
                  <template #default="{ row }">
                    <div class="date-col">
                      <span v-if="row.joinPartyDate">入党：{{ row.joinPartyDate }}</span>
                      <span v-if="row.officialPartyDate" class="text-muted">转正：{{ row.officialPartyDate }}</span>
                      <span v-if="!row.joinPartyDate" class="text-muted">待吸收发展</span>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="现所在党组织及职务" min-width="260">
                  <template #default="{ row }">
                    <div class="branch-cell">
                      <span class="branch-title">
                        {{ row.branchName }}
                        <el-tag v-if="isCrossCompany(row.branchName, row.deptName)" size="small" type="warning" effect="plain" style="margin-left: 6px">跨单位挂靠/派驻</el-tag>
                      </span>
                      <span class="dept-title">
                        <span v-if="isCrossCompany(row.branchName, row.deptName)" style="color: #e6a23c; font-weight: 500">[人事单位] </span>
                        {{ row.deptName }} · {{ row.jobTitle }}
                      </span>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="骨干特色" width="170">
                  <template #default="{ row }">
                    <div class="tags-wrapper">
                      <el-tag v-if="row.isFrontline" size="small" type="success">生产一线</el-tag>
                      <el-tag v-if="row.isTechnicalTalent" size="small" type="primary">数字骨干</el-tag>
                      <el-tag v-if="row.isDualCultivate" size="small" type="warning">双培养</el-tag>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="党费缴纳" width="110">
                  <template #default="{ row }">
                    <el-tag size="small" type="success" effect="plain">
                      <el-icon><Check /></el-icon> 按月正常
                    </el-tag>
                  </template>
                </el-table-column>

                <el-table-column label="年度集中培训时长" width="150">
                  <template #default="{ row }">
                    <div style="display: flex; flex-direction: column; gap: 2px">
                      <el-tag size="small" :type="(row.studyHours || 0) >= (row.studyTarget || 40) ? 'success' : 'warning'">
                        {{ row.studyHours || 0 }} / {{ row.studyTarget || 40 }} 学时
                      </el-tag>
                      <span style="font-size: 10.5px; color: #909399">{{ (row.studyHours || 0) >= (row.studyTarget || 40) ? '培训已达标' : '待集中补训' }}</span>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="全国党员编码" width="180">
                  <template #default="{ row }">
                    <span class="code-font">{{ row.nationalCode }}</span>
                  </template>
                </el-table-column>

                <el-table-column label="操作" width="110" fixed="right">
                  <template #default="{ row }">
                    <el-button 
                      v-if="hasPermission('roster:edit')" 
                      type="primary" 
                      size="small" 
                      icon="Edit" 
                      @click="openEditMemberDialog(row)"
                    >
                      修改档案
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>

          <!-- 标签页 2.5: 党员转接及调整备案 (转接台账 + 职务调整备案) -->
          <el-tab-pane v-if="hasPermission('roster:view') || hasPermission('workbench:transfer')" name="transfer_filing">
            <template #label>
              <span class="tab-label"><el-icon><Switch /></el-icon> 党员转接及调整备案</span>
            </template>

            <div class="transfer-filing-container">
              <!-- 顶部子模块切换栏与全局说明 -->
              <div class="sub-tab-nav-bar">
                <el-radio-group v-model="transferActiveSubTab" size="large">
                  <el-radio-button label="transfer">
                    <el-icon><Switch /></el-icon> 党员组织关系转接记录 (转入/转出)
                  </el-radio-button>
                  <el-radio-button label="adjustment">
                    <el-icon><Tickets /></el-icon> 党员党内职务调整备案
                  </el-radio-button>
                </el-radio-group>
                <div class="sub-nav-tips">
                  <span v-if="transferActiveSubTab === 'transfer'">
                    <el-tag size="small" type="success" effect="plain">业务联动机制</el-tag>
                    办理组织关系转入自动在花名册建档，转出则自动从花名册除名注销。
                  </span>
                  <span v-else>
                    <el-tag size="small" type="primary" effect="plain">业务联动机制</el-tag>
                    党内职务调整备案生效后，自动同步更新对应党员花名册中的党内职务。
                  </span>
                </div>
              </div>

              <!-- ============================================== -->
              <!-- 子模块 1: 党员组织关系转接记录 (转入 / 转出) -->
              <!-- ============================================== -->
              <div v-if="transferActiveSubTab === 'transfer'" class="transfer-records-section">
                <!-- 统计卡片横幅 -->
                <div class="roster-stats-banner">
                  <div class="stat-pill">
                    <span class="pill-label">累计转接人次</span>
                    <span class="pill-val">{{ transfersList.length }} 人次</span>
                  </div>
                  <div class="stat-pill">
                    <span class="pill-label">转入本级在册</span>
                    <span class="pill-val color-green">{{ transferInCount }} 人</span>
                  </div>
                  <div class="stat-pill">
                    <span class="pill-label">转出外部党组织</span>
                    <span class="pill-val color-red">{{ transferOutCount }} 人</span>
                  </div>
                  <div class="stat-pill">
                    <span class="pill-label">花名册实时联动</span>
                    <span class="pill-val color-blue">100% 自动同步</span>
                  </div>
                  <div class="stat-pill">
                    <span class="pill-label">介绍信存根归档率</span>
                    <span class="pill-val color-gold">100%</span>
                  </div>
                </div>

                <!-- 工具栏 -->
                <div class="table-toolbar">
                  <div class="toolbar-left">
                    <el-input 
                      v-model="transferSearchKeyword" 
                      placeholder="搜索党员姓名、工号、介绍信编号或党组织..." 
                      prefix-icon="Search"
                      clearable
                      style="width: 280px"
                    />
                    <el-select v-model="transferTypeFilter" placeholder="转接类型" clearable style="width: 140px">
                      <el-option label="全部转接类型" value="" />
                      <el-option label="组织关系转入" :value="1" />
                      <el-option label="组织关系转出" :value="2" />
                    </el-select>
                    <el-select v-model="transferBranchFilter" placeholder="所属/关联党支部" clearable style="width: 230px">
                      <el-option label="全部党组织" value="" />
                      <el-option label="红数信息支部" value="中共红河红数信息技术服务有限公司支部委员会" />
                      <el-option label="幂次科技支部" value="中共云南幂次科技有限公司支部委员会" />
                      <el-option label="链达科技支部" value="中共红河链达科技有限公司支部委员会" />
                      <el-option label="集团党总支" value="中共红河数据产业集团有限公司总支部委员会" />
                    </el-select>
                    <el-button v-if="transferSearchKeyword || transferTypeFilter || transferBranchFilter" link type="primary" @click="resetTransferFilters">
                      重置
                    </el-button>
                  </div>
                  <div class="toolbar-right">
                    <el-button 
                      v-if="hasPermission('workbench:transfer') || hasPermission('roster:create')" 
                      type="success" 
                      icon="Plus" 
                      @click="openTransferInDialog"
                    >
                      办理组织关系转入
                    </el-button>
                    <el-button 
                      v-if="hasPermission('workbench:transfer') || hasPermission('roster:edit')" 
                      type="danger" 
                      icon="Right" 
                      @click="openTransferOutDialog"
                    >
                      办理组织关系转出
                    </el-button>
                    <el-button icon="Download" @click="exportTransferExcel">导出转接台账</el-button>
                  </div>
                </div>

                <!-- 转接记录表格 -->
                <el-table :data="filteredTransfersList" style="width: 100%" stripe class="custom-dj-table">
                  <el-table-column label="转接类型" width="120">
                    <template #default="{ row }">
                      <el-tag :type="row.transferType === 1 ? 'success' : 'danger'" effect="dark">
                        {{ row.transferType === 1 ? '组织关系转入' : '组织关系转出' }}
                      </el-tag>
                    </template>
                  </el-table-column>

                  <el-table-column label="党员姓名" width="130">
                    <template #default="{ row }">
                      <div class="roster-name-cell">
                        <el-avatar :size="30" :class="row.transferType === 1 ? 'avatar-green' : 'avatar-gray'">
                          {{ (row.memberName || '').slice(0, 1) }}
                        </el-avatar>
                        <div>
                          <strong>{{ row.memberName }}</strong>
                          <div class="sub-workno">{{ row.workNo }}</div>
                        </div>
                      </div>
                    </template>
                  </el-table-column>

                  <el-table-column label="政治面貌" width="105">
                    <template #default="{ row }">
                      <el-tag size="small" :type="row.partyStatus === 1 ? 'danger' : 'warning'">
                        {{ row.partyStatus === 1 ? '正式党员' : '预备党员' }}
                      </el-tag>
                    </template>
                  </el-table-column>

                  <el-table-column label="原所在党组织 (转出方)" min-width="220">
                    <template #default="{ row }">
                      <div style="font-size: 13px; font-weight: 500; color: #334155">{{ row.fromOrgName }}</div>
                    </template>
                  </el-table-column>

                  <el-table-column label="拟转入党组织 (转入方)" min-width="220">
                    <template #default="{ row }">
                      <div style="font-size: 13px; font-weight: 600; color: #1e293b">{{ row.toOrgName }}</div>
                    </template>
                  </el-table-column>

                  <el-table-column label="介绍信凭证编号" width="170">
                    <template #default="{ row }">
                      <span class="code-font">{{ row.letterNo || '—' }}</span>
                    </template>
                  </el-table-column>

                  <el-table-column label="转接日期" width="120">
                    <template #default="{ row }">
                      <span style="font-size: 12.5px; color: #475569">{{ row.transferDate }}</span>
                    </template>
                  </el-table-column>

                  <el-table-column label="转接原因 / 事由" min-width="170">
                    <template #default="{ row }">
                      <span style="font-size: 12px; color: #64748b">{{ row.transferReason || '正常组织关系流转' }}</span>
                    </template>
                  </el-table-column>

                  <el-table-column label="经办人" width="100">
                    <template #default="{ row }">
                      <span style="font-size: 12.5px">{{ row.operatorName || '组织员' }}</span>
                    </template>
                  </el-table-column>

                  <el-table-column label="花名册联动" width="130">
                    <template #default="{ row }">
                      <el-tag v-if="row.transferType === 1" size="small" type="success" effect="plain">
                        <el-icon><Check /></el-icon> 已同步名册
                      </el-tag>
                      <el-tag v-else size="small" type="danger" effect="plain">
                        <el-icon><Close /></el-icon> 已从名册除名
                      </el-tag>
                    </template>
                  </el-table-column>

                  <el-table-column label="操作" width="115" fixed="right">
                    <template #default="{ row }">
                      <el-button link type="primary" size="small" icon="Document" @click="viewTransferRecord(row)">
                        介绍信详情
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>

              <!-- ============================================== -->
              <!-- 子模块 2: 党员党内职务调整备案 -->
              <!-- ============================================== -->
              <div v-if="transferActiveSubTab === 'adjustment'" class="adjustment-records-section">
                <!-- 统计卡片横幅 -->
                <div class="roster-stats-banner">
                  <div class="stat-pill">
                    <span class="pill-label">职务调整备案总数</span>
                    <span class="pill-val">{{ adjustmentsList.length }} 次</span>
                  </div>
                  <div class="stat-pill">
                    <span class="pill-label">在任支部书记/副书记</span>
                    <span class="pill-val color-red">{{ secretaryCount }} 人</span>
                  </div>
                  <div class="stat-pill">
                    <span class="pill-label">支委会班子委员</span>
                    <span class="pill-val color-orange">{{ committeeCount }} 人</span>
                  </div>
                  <div class="stat-pill">
                    <span class="pill-label">批文归档规范率</span>
                    <span class="pill-val color-green">100% (文号完整)</span>
                  </div>
                  <div class="stat-pill">
                    <span class="pill-label">花名册职务联动</span>
                    <span class="pill-val color-blue">实时自动同步</span>
                  </div>
                </div>

                <!-- 工具栏 -->
                <div class="table-toolbar">
                  <div class="toolbar-left">
                    <el-input 
                      v-model="adjustmentSearchKeyword" 
                      placeholder="搜索党员姓名、工号、批文号或职务..." 
                      prefix-icon="Search"
                      clearable
                      style="width: 280px"
                    />
                    <el-select v-model="adjustmentBranchFilter" placeholder="按任职党组织" clearable style="width: 240px">
                      <el-option label="全部所属党组织" value="" />
                      <el-option label="红数信息支部" value="中共红河红数信息技术服务有限公司支部委员会" />
                      <el-option label="幂次科技支部" value="中共云南幂次科技有限公司支部委员会" />
                      <el-option label="链达科技支部" value="中共红河链达科技有限公司支部委员会" />
                      <el-option label="集团党总支" value="中共红河数据产业集团有限公司总支部委员会" />
                    </el-select>
                    <el-select v-model="adjustmentPostFilter" placeholder="职务类型" clearable style="width: 150px">
                      <el-option label="全部党内职务" value="" />
                      <el-option label="党总支书记" value="党总支书记" />
                      <el-option label="党总支副书记" value="党总支副书记" />
                      <el-option label="党支部书记" value="党支部书记" />
                      <el-option label="支部副书记" value="支部副书记" />
                      <el-option label="组织委员" value="组织委员" />
                      <el-option label="宣传委员" value="宣传委员" />
                      <el-option label="纪检委员" value="纪检委员" />
                    </el-select>
                    <el-button v-if="adjustmentSearchKeyword || adjustmentBranchFilter || adjustmentPostFilter" link type="primary" @click="resetAdjustmentFilters">
                      重置
                    </el-button>
                  </div>
                  <div class="toolbar-right">
                    <el-button 
                      v-if="hasPermission('workbench:transfer') || hasPermission('roster:edit')" 
                      type="primary" 
                      icon="Plus" 
                      @click="openAdjustmentDialog"
                    >
                      新增职务调整备案
                    </el-button>
                    <el-button icon="Download" @click="exportAdjustmentExcel">导出调整台账</el-button>
                  </div>
                </div>

                <!-- 职务调整表格 -->
                <el-table :data="filteredAdjustmentsList" style="width: 100%" stripe class="custom-dj-table">
                  <el-table-column label="党员姓名" width="140">
                    <template #default="{ row }">
                      <div class="roster-name-cell">
                        <el-avatar :size="30" class="avatar-red">{{ (row.memberName || '').slice(0, 1) }}</el-avatar>
                        <div>
                          <strong>{{ row.memberName }}</strong>
                          <div class="sub-workno">{{ row.workNo }}</div>
                        </div>
                      </div>
                    </template>
                  </el-table-column>

                  <el-table-column label="任职党组织" min-width="220">
                    <template #default="{ row }">
                      <div style="font-size: 13px; font-weight: 500; color: #1e293b">{{ row.orgName }}</div>
                    </template>
                  </el-table-column>

                  <el-table-column label="调整前职务" width="130">
                    <template #default="{ row }">
                      <el-tag size="small" type="info" effect="plain">{{ row.oldPost || '普通党员' }}</el-tag>
                    </template>
                  </el-table-column>

                  <el-table-column label="调整后职务 (新任)" width="150">
                    <template #default="{ row }">
                      <el-tag size="small" type="danger" effect="dark" style="font-weight: 600">
                        {{ row.newPost }}
                      </el-tag>
                    </template>
                  </el-table-column>

                  <el-table-column label="调整类型" width="110">
                    <template #default="{ row }">
                      <el-tag size="small" type="primary" effect="plain">{{ row.adjustType || '任职任命' }}</el-tag>
                    </template>
                  </el-table-column>

                  <el-table-column label="批准文号 / 批复号" min-width="190">
                    <template #default="{ row }">
                      <span class="doc-code-badge">{{ row.documentNo }}</span>
                    </template>
                  </el-table-column>

                  <el-table-column label="发文 / 生效日期" width="130">
                    <template #default="{ row }">
                      <span style="font-size: 12.5px; color: #475569">{{ row.effectiveDate }}</span>
                    </template>
                  </el-table-column>

                  <el-table-column label="批准机关 / 决定单位" min-width="200">
                    <template #default="{ row }">
                      <span style="font-size: 12px; color: #64748b">{{ row.approvalUnit }}</span>
                    </template>
                  </el-table-column>

                  <el-table-column label="职责分工" min-width="190">
                    <template #default="{ row }">
                      <span style="font-size: 12px; color: #475569">{{ row.dutyDescription || '按党章分工履职' }}</span>
                    </template>
                  </el-table-column>

                  <el-table-column label="备案人" width="100">
                    <template #default="{ row }">
                      <span style="font-size: 12.5px">{{ row.operatorName || '组织员' }}</span>
                    </template>
                  </el-table-column>

                  <el-table-column label="操作" width="115" fixed="right">
                    <template #default="{ row }">
                      <el-button link type="primary" size="small" icon="Document" @click="viewAdjustmentRecord(row)">
                        查看备案表
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </el-tab-pane>

          <!-- 标签页 3: 支部“三会一课”与组织生活台账 (组织员/支部书记) -->
          <el-tab-pane v-if="hasPermission('meeting:view')" name="meetings">
            <template #label>
              <span class="tab-label"><el-icon><Calendar /></el-icon> “三会一课”/组织生活</span>
            </template>

            <div class="meetings-mgr-container">
              <!-- 顶部三会一课统计卡片 -->
              <div class="roster-stats-banner">
                <div class="stat-pill">
                  <span class="pill-label">支部委员会 (支委会)</span>
                  <span class="pill-val color-red">36 / 36 次</span>
                </div>
                <div class="stat-pill">
                  <span class="pill-label">支部党员大会</span>
                  <span class="pill-val color-orange">12 / 12 次</span>
                </div>
                <div class="stat-pill">
                  <span class="pill-label">专题党课</span>
                  <span class="pill-val color-blue">12 / 12 次</span>
                </div>
                <div class="stat-pill">
                  <span class="pill-label">每月主题党日</span>
                  <span class="pill-val color-gold">36 / 36 期</span>
                </div>
                <div class="stat-pill">
                  <span class="pill-label">平均到会出席率</span>
                  <span class="pill-val color-green">98.8%</span>
                </div>
                <div class="stat-pill">
                  <span class="pill-label">纪要归档完整率</span>
                  <span class="pill-val color-green">100%</span>
                </div>
              </div>

              <!-- 搜索与筛选工具栏 (支持单选、多选与标签精准筛选) -->
              <div class="table-toolbar">
                <div class="toolbar-left">
                  <el-input 
                    v-model="meetingKeyword" 
                    placeholder="搜索会议议题、主持人、纪实..." 
                    prefix-icon="Search"
                    clearable
                    style="width: 220px"
                  />
                  <!-- 支持单选/多选所属党组织 -->
                  <el-select 
                    v-model="meetingBranchFilter" 
                    multiple 
                    collapse-tags 
                    collapse-tags-max="1" 
                    placeholder="党组织筛选(支持多选/单选)" 
                    clearable 
                    style="width: 230px"
                  >
                    <el-option label="红数信息支部 (云服务/网络安全)" value="中共红河红数信息技术服务有限公司支部委员会" />
                    <el-option label="幂次科技支部 (软件开发/数字化)" value="中共云南幂次科技有限公司支部委员会" />
                    <el-option label="链达科技支部 (城市综合体运营)" value="中共红河链达科技有限公司支部委员会" />
                    <el-option label="集团党总支" value="中共红河数据产业集团有限公司总支部委员会" />
                  </el-select>
                  <!-- 支持单选/多选组织生活类型 -->
                  <el-select 
                    v-model="meetingTypeFilter" 
                    multiple 
                    collapse-tags 
                    collapse-tags-max="1" 
                    placeholder="组织生活类型(支持多选/单选)" 
                    clearable 
                    style="width: 210px"
                  >
                    <el-option label="支委会" :value="1" />
                    <el-option label="支部党员大会" :value="2" />
                    <el-option label="专题党课" :value="3" />
                    <el-option label="主题党日" :value="4" />
                  </el-select>
                  <!-- 新增：支持通过议题彩色标签单选/多选筛选 -->
                  <el-select 
                    v-model="meetingTagFilter" 
                    multiple 
                    collapse-tags 
                    collapse-tags-max="1" 
                    placeholder="议题标签筛选(支持多选/单选)" 
                    clearable 
                    style="width: 210px"
                  >
                    <el-option 
                      v-for="tag in availableTopicTags" 
                      :key="tag.id" 
                      :label="tag.name" 
                      :value="tag.name"
                    >
                      <div style="display: flex; align-items: center; gap: 6px">
                        <span :style="{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: tag.color }"></span>
                        <span>{{ tag.name }}</span>
                      </div>
                    </el-option>
                  </el-select>
                  <el-button v-if="meetingKeyword || (meetingBranchFilter && meetingBranchFilter.length) || (meetingTypeFilter && meetingTypeFilter.length) || (meetingTagFilter && meetingTagFilter.length)" link type="primary" @click="resetMeetingFilters">
                    重置筛选
                  </el-button>
                </div>

                <div class="toolbar-right">
                  <el-button v-if="hasPermission('meeting:create')" type="primary" icon="Plus" @click="openAddMeetingDialog">记录组织生活会议</el-button>
                  <el-button v-if="hasPermission('meeting:tags_manage')" type="warning" plain icon="CollectionTag" @click="openManageTagsDialog">管理议题标签</el-button>
                  <el-button v-if="hasPermission('meeting:export')" icon="Download" @click="exportMeetingsExcel">导出组织生活台账</el-button>
                </div>
              </div>

              <!-- 会议台账表格 -->
              <el-table :data="filteredMeetingsList" style="width: 100%" stripe class="custom-dj-table">
                <el-table-column label="组织生活类型" width="130">
                  <template #default="{ row }">
                    <el-tag :type="getMeetingTypeTag(row.meetingType)" effect="dark">
                      {{ row.meetingTypeName }}
                    </el-tag>
                  </template>
                </el-table-column>

                <el-table-column label="会议主要议题及重点研究事项" min-width="290">
                  <template #default="{ row }">
                    <div style="display: flex; flex-direction: column; gap: 4px">
                      <strong>{{ row.title }}</strong>
                      <!-- 重点研究议题与彩色标签 -->
                      <div v-if="row.agendaItems && row.agendaItems.length" style="display: flex; flex-direction: column; gap: 3px; margin: 2px 0">
                        <div v-for="(ag, agIdx) in row.agendaItems" :key="agIdx" style="display: flex; align-items: center; gap: 6px; font-size: 11.5px">
                          <el-tag size="small" :color="ag.tagColor" effect="dark" style="border: none; color: #fff; font-size: 10px; height: 18px; line-height: 18px; padding: 0 5px">
                            {{ ag.tagName }}
                          </el-tag>
                          <span style="color: #334155">{{ ag.topic }}</span>
                        </div>
                      </div>
                      <span style="font-size: 11px; color: #94a3b8; line-height: 1.4">{{ row.content }}</span>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="所属党组织" min-width="190">
                  <template #default="{ row }">
                    <div style="display: flex; flex-direction: column">
                      <span style="font-weight: 600; font-size: 12.5px">{{ row.branchShort }}</span>
                      <span style="font-size: 11px; color: #909399">{{ row.branchName }}</span>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="开会时间与地点" width="170">
                  <template #default="{ row }">
                    <div style="display: flex; flex-direction: column; font-size: 12px">
                      <span><el-icon><Calendar /></el-icon> {{ row.date }}</span>
                      <span style="color: #909399; font-size: 11px">{{ row.place }}</span>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="主持人 / 主讲人" width="140">
                  <template #default="{ row }">
                    <div style="font-size: 12px">
                      <div>{{ row.moderator }}</div>
                      <div v-if="row.speaker" style="color: #e6a23c; font-size: 11px">主讲：{{ row.speaker }}</div>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="参会考勤" width="130">
                  <template #default="{ row }">
                    <div style="display: flex; flex-direction: column; gap: 2px">
                      <span style="font-size: 12px">到会：<strong>{{ row.actualCount }}</strong> / {{ row.expectedCount }}人</span>
                      <el-progress :percentage="row.attendanceRate" :stroke-width="5" :show-text="false" color="#67c23a" />
                      <span style="font-size: 10px; color: #67c23a">到会率 {{ row.attendanceRate }}%</span>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="发展党员业务联动" width="170">
                  <template #default="{ row }">
                    <el-tag v-if="row.relatedStep > 0" type="danger" effect="plain" size="small">
                      联动第 {{ row.relatedStep }} 步 ({{ row.relatedMember }})
                    </el-tag>
                    <span v-else style="color: #909399; font-size: 11.5px">常规党内生活</span>
                  </template>
                </el-table-column>

                <el-table-column label="操作与附件归档" width="310" fixed="right">
                  <template #default="{ row }">
                    <div style="display: flex; gap: 6px; align-items: center">
                      <el-button 
                        v-if="hasPermission('meeting:edit')" 
                        type="primary" 
                        size="small" 
                        icon="Edit" 
                        @click="openEditMeetingDialog(row)"
                      >
                        修改
                      </el-button>
                      <el-button 
                        v-if="hasPermission('meeting:delete')" 
                        type="danger" 
                        size="small" 
                        icon="Delete" 
                        link
                        @click="deleteMeetingRecord(row)"
                      >
                        删除
                      </el-button>
                      <el-button size="small" icon="Paperclip" @click="openMeetingAttachmentsDialog(row)">
                        附件 ({{ (row.attachments && row.attachments.length) || 3 }}件)
                      </el-button>
                      <el-button link type="success" size="small" icon="Download" @click="downloadMeetingDoc(row)">
                        下载纪要
                      </el-button>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>

          <!-- 标签页 4: 组织与个人奖惩/荣誉台账 (纪检/组织员/书记) -->
          <el-tab-pane v-if="hasPermission('honor:view')" name="honors">
            <template #label>
              <span class="tab-label"><el-icon><Trophy /></el-icon> 组织/个人奖惩或荣誉</span>
            </template>

            <div class="honors-container-card">
              <!-- 顶部奖惩荣誉统计卡片 -->
              <div class="roster-stats-banner">
                <div class="stat-pill">
                  <span class="pill-label">组织荣誉表彰</span>
                  <span class="pill-val color-red">{{ getHonorCount(2, 1) }} 项</span>
                </div>
                <div class="stat-pill">
                  <span class="pill-label">个人荣誉表彰</span>
                  <span class="pill-val color-gold">{{ getHonorCount(1, 1) }} 项</span>
                </div>
                <div class="stat-pill">
                  <span class="pill-label">纪律处分 / 诫勉批评</span>
                  <span class="pill-val color-orange">{{ getHonorCount(null, 2) }} 项</span>
                </div>
                <div class="stat-pill">
                  <span class="pill-label">省部级及以上表彰</span>
                  <span class="pill-val color-red">2 项</span>
                </div>
                <div class="stat-pill">
                  <span class="pill-label">整改清零闭环率</span>
                  <span class="pill-val color-green">100%</span>
                </div>
              </div>

              <!-- 搜索与筛选工具栏 -->
              <div class="table-toolbar">
                <div class="toolbar-left">
                  <el-input 
                    v-model="honorKeyword" 
                    placeholder="搜索荣誉/处分名称、获得者、文号..." 
                    prefix-icon="Search"
                    clearable
                    style="width: 240px"
                  />
                  <!-- 奖惩分类筛选：组织 vs 个人 -->
                  <el-select v-model="honorCategoryFilter" placeholder="奖惩主体分类" clearable style="width: 170px">
                    <el-option label="全部主体分类" :value="null" />
                    <el-option label="组织奖惩/荣誉" :value="2" />
                    <el-option label="个人奖惩/荣誉" :value="1" />
                  </el-select>
                  <!-- 奖惩类型筛选：荣誉表彰 vs 处分惩戒 -->
                  <el-select v-model="honorRecordTypeFilter" placeholder="奖惩类型" clearable style="width: 160px">
                    <el-option label="全部奖惩类型" :value="null" />
                    <el-option label="荣誉表彰" :value="1" />
                    <el-option label="纪律处分/诫勉" :value="2" />
                  </el-select>
                  <!-- 所属党支部筛选 -->
                  <el-select v-model="honorBranchFilter" multiple collapse-tags collapse-tags-max="1" placeholder="党组织筛选(可多选)" clearable style="width: 220px">
                    <el-option label="红数信息支部" value="中共红河红数信息技术服务有限公司支部委员会" />
                    <el-option label="幂次科技支部" value="中共云南幂次科技有限公司支部委员会" />
                    <el-option label="链达科技支部" value="中共红河链达科技有限公司支部委员会" />
                    <el-option label="集团党总支" value="中共红河数据产业集团有限公司总支部委员会" />
                  </el-select>
                  <!-- 级别筛选 -->
                  <el-select v-model="honorLevelFilter" placeholder="表彰/处分级别" clearable style="width: 150px">
                    <el-option label="全部级别" value="" />
                    <el-option label="国家级" value="国家级" />
                    <el-option label="省部级" value="省部级" />
                    <el-option label="州级/市级" value="州级/市级" />
                    <el-option label="集团级" value="集团级" />
                    <el-option label="支部级" value="支部级" />
                  </el-select>
                  <el-button v-if="honorKeyword || honorCategoryFilter !== null || honorRecordTypeFilter !== null || (honorBranchFilter && honorBranchFilter.length) || honorLevelFilter" link type="primary" @click="resetHonorFilters">
                    重置筛选
                  </el-button>
                </div>

                <div class="toolbar-right">
                  <el-button v-if="hasPermission('honor:create')" type="primary" icon="Plus" @click="openAddHonorDialog">登记奖惩/荣誉</el-button>
                  <el-button v-if="hasPermission('honor:export')" icon="Download" @click="exportHonorsExcel">导出荣誉台账</el-button>
                </div>
              </div>

              <!-- 奖惩荣誉数据表格 -->
              <el-table :data="filteredHonorsList" style="width: 100%" stripe class="custom-dj-table">
                <el-table-column label="奖惩主体分类" width="140">
                  <template #default="{ row }">
                    <el-tag :type="row.category === 2 ? 'warning' : 'primary'" effect="dark">
                      <el-icon v-if="row.category === 2"><OfficeBuilding /></el-icon>
                      <el-icon v-else><User /></el-icon>
                      {{ row.category === 2 ? '组织荣誉/奖惩' : '个人荣誉/奖惩' }}
                    </el-tag>
                  </template>
                </el-table-column>

                <el-table-column label="奖惩性质" width="130">
                  <template #default="{ row }">
                    <el-tag :type="row.recordType === 1 ? 'success' : 'danger'" effect="plain">
                      <el-icon v-if="row.recordType === 1"><Trophy /></el-icon>
                      <el-icon v-else><Warning /></el-icon>
                      {{ row.recordTypeName }}
                    </el-tag>
                  </template>
                </el-table-column>

                <el-table-column label="表彰/处分名称及主要事迹" min-width="280">
                  <template #default="{ row }">
                    <div style="display: flex; flex-direction: column; gap: 4px">
                      <strong :style="{ color: row.recordType === 1 ? '#c21c1d' : '#e6a23c', fontSize: '13.5px' }">
                        {{ row.title }}
                      </strong>
                      <span style="font-size: 11.5px; color: #64748b; line-height: 1.4">{{ row.reasonContent }}</span>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="获奖/受处分主体" min-width="170">
                  <template #default="{ row }">
                    <div style="display: flex; align-items: center; gap: 6px">
                      <strong style="color: #1e293b">{{ row.targetName }}</strong>
                      <el-tag v-if="row.workNo" size="small" type="info">{{ row.workNo }}</el-tag>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="所属党组织" min-width="190">
                  <template #default="{ row }">
                    <div style="display: flex; flex-direction: column">
                      <span style="font-weight: 600; font-size: 12.5px">{{ row.orgShort }}</span>
                      <span style="font-size: 11px; color: #94a3b8">{{ row.orgName }}</span>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="表彰级别" width="110">
                  <template #default="{ row }">
                    <el-tag size="small" :type="getHonorLevelTag(row.level)">
                      {{ row.level }}
                    </el-tag>
                  </template>
                </el-table-column>

                <el-table-column label="决定/授予单位" min-width="180" prop="grantOrg" show-overflow-tooltip />

                <el-table-column label="决定文号" width="160">
                  <template #default="{ row }">
                    <span class="code-font" style="font-size: 11.5px">{{ row.docNo }}</span>
                  </template>
                </el-table-column>

                <el-table-column label="决定日期" width="120" prop="recordDate" />

                <el-table-column label="操作" width="160" fixed="right">
                  <template #default="{ row }">
                    <el-button 
                      v-if="hasPermission('honor:edit')" 
                      type="primary" 
                      size="small" 
                      icon="Edit" 
                      @click="openEditHonorDialog(row)"
                    >
                      修改
                    </el-button>
                    <el-button 
                      v-if="hasPermission('honor:delete')" 
                      type="danger" 
                      size="small" 
                      icon="Delete" 
                      link 
                      @click="deleteHonorItem(row)"
                    >
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>

          <!-- 标签页 5: 25步全景规范与文书套打指南 (总支组织员/支部书记) -->
          <el-tab-pane v-if="hasPermission('template:view')" name="templates">
            <template #label>
              <span class="tab-label"><el-icon><DocumentCopy /></el-icon> 文书知识库</span>
            </template>

            <div class="templates-mgr-container">
              <div class="mgr-header-card">
                <div class="mgr-header-left">
                  <h3><el-icon><FolderOpened /></el-icon> 发展党员 25 步全套文书模板管理库（双轨制支持）</h3>
                  <p>系统已内置全部 25 个步骤中组部官方标准模板；同时支持集团管理员按红河数据产业集团红头格式自主<strong>导入/更新自定义模板</strong>。套打时自动优先采用已启用的自定义模板，并支持一键恢复官方标准！</p>
                </div>
                <div class="mgr-header-right">
                  <el-tag type="success" effect="dark" size="large">内置官方默认库：25 份</el-tag>
                  <el-tag type="warning" effect="dark" size="large">已导入企业定制版：{{ customizedTemplatesCount }} 份</el-tag>
                </div>
              </div>

              <!-- 模板列表表格 -->
              <el-table :data="templatesList" style="width: 100%" stripe class="custom-dj-table">
                <el-table-column label="步骤编码" width="110">
                  <template #default="{ row }">
                    <el-tag size="small" type="danger" effect="dark">第 {{ row.stepId }} 步</el-tag>
                  </template>
                </el-table-column>

                <el-table-column label="模板名称及标准范式" min-width="220">
                  <template #default="{ row }">
                    <div class="tpl-name-cell">
                      <strong>{{ row.name }}</strong>
                      <span class="tpl-code-text">{{ row.code }}</span>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="当前启用版本状态" width="180">
                  <template #default="{ row }">
                    <div class="tpl-status-cell">
                      <el-tag v-if="row.isCustomized" type="warning" effect="dark">
                        <el-icon><EditPen /></el-icon> 企业定制版
                      </el-tag>
                      <el-tag v-else type="info" effect="plain">
                        <el-icon><Document /></el-icon> 官方标准版
                      </el-tag>
                      <span class="version-tag">{{ row.version }}</span>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="当前文件说明" min-width="240">
                  <template #default="{ row }">
                    <div class="tpl-file-meta">
                      <span v-if="row.isCustomized" class="custom-file-name">
                        <el-icon><Files /></el-icon> {{ row.customName }}
                      </span>
                      <span v-else class="default-file-name">
                        <el-icon><Document /></el-icon> {{ row.defaultName }}
                      </span>
                      <span class="update-sub">{{ row.updateUser }} · {{ row.updateTime }}</span>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="模板维护与套打操作" width="340" fixed="right">
                  <template #default="{ row }">
                    <el-button size="small" link type="primary" icon="Download" @click="downloadCurrentTemplate(row)">
                      下载使用中模板
                    </el-button>
                    <el-button size="small" link type="info" icon="Document" @click="downloadDefaultTemplate(row)">
                      下载官方默认
                    </el-button>
                    <el-button 
                      v-if="hasPermission('template:upload')" 
                      size="small" 
                      link 
                      type="warning" 
                      icon="Upload" 
                      @click="openUploadDialog(row)"
                    >
                      管理员导入
                    </el-button>
                    <el-button 
                      v-if="row.isCustomized && hasPermission('template:reset')" 
                      size="small" 
                      link 
                      type="danger" 
                      icon="RefreshLeft" 
                      @click="restoreDefaultTemplate(row)"
                    >
                      恢复默认
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>

          <!-- 标签页 6: 国企年度指标与结构驾驶舱 (全员可查阅) -->
          <el-tab-pane v-if="hasPermission('cockpit:view')" name="cockpit">
            <template #label>
              <span class="tab-label"><el-icon><DataAnalysis /></el-icon> 年度发展指标与结构驾驶舱</span>
            </template>

            <div class="cockpit-container">
              <!-- 统计指标四卡片 -->
              <div class="metrics-grid">
                <div v-for="(metric, idx) in ANNUAL_QUOTA.metrics" :key="idx" class="metric-card">
                  <div class="metric-card-top">
                    <span class="metric-title">{{ metric.label }}</span>
                    <el-tag size="small" type="success">达标</el-tag>
                  </div>
                  <div class="metric-numbers">
                    <span class="actual-val">{{ metric.actual }}%</span>
                    <span class="target-val">国资委标准 ≥ {{ metric.target }}%</span>
                  </div>
                  <el-progress 
                    :percentage="metric.actual" 
                    :color="idx === 3 ? '#e6a23c' : '#c21c1d'" 
                    :stroke-width="8" 
                    :show-text="false" 
                  />
                  <div class="metric-desc">{{ metric.desc }}</div>
                </div>
              </div>

              <!-- 图表与结构分析区 -->
              <div class="charts-row">
                <div class="chart-box">
                  <div class="chart-header">
                    <h4><el-icon><PieChart /></el-icon> 2025年度发展党员阶段分布漏斗</h4>
                    <span class="plan-stat">总发展规划：{{ ANNUAL_QUOTA.totalPlan }} 人 / 已完成：{{ ANNUAL_QUOTA.approvedCount }} 人</span>
                  </div>
                  <div class="funnel-container">
                    <div class="funnel-stage stage-1">
                      <span class="stage-lbl">① 申请入党人库</span>
                      <span class="stage-val">22 人 (蓄水池)</span>
                    </div>
                    <div class="funnel-stage stage-2">
                      <span class="stage-lbl">② 确定培养入党积极分子</span>
                      <span class="stage-val">14 人 (考察中)</span>
                    </div>
                    <div class="funnel-stage stage-3">
                      <span class="stage-lbl">③ 确定发展对象 (政审/培训)</span>
                      <span class="stage-val">6 人 (重点把关)</span>
                    </div>
                    <div class="funnel-stage stage-4">
                      <span class="stage-lbl">④ 预备党员接收 (大会讨论)</span>
                      <span class="stage-val">5 人 (待上总支会)</span>
                    </div>
                    <div class="funnel-stage stage-5">
                      <span class="stage-lbl">⑤ 预备党员转正 (归档)</span>
                      <span class="stage-val">7 人 (已按期转正)</span>
                    </div>
                  </div>
                </div>

                <div class="chart-box">
                  <div class="chart-header">
                    <h4><el-icon><Histogram /></el-icon> 三大子公司党支部发展进度与指标达标对比</h4>
                  </div>
                  <div class="branch-progress-list">
                    <div class="branch-item">
                      <div class="b-info">
                        <span>红数信息党支部 (云服务、运维服务、网络及安全服务)</span>
                        <strong>3 / 4 人 (75%)</strong>
                      </div>
                      <el-progress :percentage="75" color="#c21c1d" />
                    </div>
                    <div class="branch-item">
                      <div class="b-info">
                        <span>幂次科技党支部 (软件开发、平台运营、企业数字化转型支撑)</span>
                        <strong>5 / 5 人 (100% 满额)</strong>
                      </div>
                      <el-progress :percentage="100" color="#67c23a" />
                    </div>
                    <div class="branch-item">
                      <div class="b-info">
                        <span>链达科技党支部 (城市综合体运营等)</span>
                        <strong>3 / 4 人 (75%)</strong>
                      </div>
                      <el-progress :percentage="75" color="#e6a23c" />
                    </div>
                  </div>
                </div>
              </div>

              <!-- 国企“双培养”机制推进台账卡片 -->
              <div class="dual-cultivate-card">
                <div class="dual-header">
                  <div class="dual-title">
                    <el-icon :size="20" color="#c21c1d"><Medal /></el-icon>
                    <strong>国企“双培养”工程落地台账（把业务骨干培养成党员，把党员培养成业务骨干）</strong>
                  </div>
                  <el-tag type="danger" effect="plain">累计入库数字高素质骨干 10 人</el-tag>
                </div>
                <el-row :gutter="16" class="dual-row">
                  <el-col :span="8">
                    <div class="dual-stat-box">
                      <div class="num-text">6 人</div>
                      <div class="sub-text">政务云运维及大数据技术骨干递交入党申请并确立培养</div>
                    </div>
                  </el-col>
                  <el-col :span="8">
                    <div class="dual-stat-box">
                      <div class="num-text">4 人</div>
                      <div class="sub-text">AI 算法科学家 / 博士研发团队带头人列入重点发展计划</div>
                    </div>
                  </el-col>
                  <el-col :span="8">
                    <div class="dual-stat-box">
                      <div class="num-text">100%</div>
                      <div class="sub-text">指定“资深技术导师 + 党务骨干”双联系人指导考察</div>
                    </div>
                  </el-col>
                </el-row>
              </div>
            </div>
          </el-tab-pane>

          <!-- 标签页 7: 党建通知中心与渠道配置 (全员查阅通知，仅管理员/组织员可配置渠道与扫描) -->
          <el-tab-pane v-if="hasPermission('notice:view')" name="notices">
            <template #label>
              <span class="tab-label">
                <el-icon><BellFilled /></el-icon> 通知中心
                <el-badge v-if="unreadNoticeCount > 0" :value="unreadNoticeCount" class="tab-badge" />
              </span>
            </template>

            <div class="notices-view-container">
              <!-- 顶部渠道状态卡片一览 (权限隔离：仅系统超级管理员 sys_admin 独占可见和配置) -->
              <div v-if="hasRole('sys_admin')" class="channel-status-cards">
                <div class="channel-card-header">
                  <div class="title-with-desc">
                    <h3><el-icon><Connection /></el-icon> 通知渠道配置（企业微信 / 钉钉 / 阿里云短信 / 邮件 / 站内信）</h3>
                    <span class="sub-tip">配置并启用渠道后，可向指定接收人发送测试消息，核对实际收件情况。</span>
                  </div>
                  <div class="actions">
                    <el-button type="danger" plain icon="Refresh" :loading="noticeBusy" @click="triggerSystemComplianceScan">
                      执行全集团合规扫描并推送
                    </el-button>
                    <el-button type="primary" icon="Promotion" @click="openSendNoticeDialog">
                      发送新党务通知
                    </el-button>
                  </div>
                </div>

                <el-alert v-if="!apiSession" title="当前为演示身份。请使用正式账号密码登录，以读取、保存配置或发送通知。" type="info" :closable="false" style="margin-bottom: 16px" />
                <div class="channel-grid">
                  <div 
                    v-for="ch in noticeChannels" 
                    :key="ch.id" 
                    class="channel-box"
                    :class="{ 'channel-disabled': ch.enabled === 0 }"
                  >
                    <div class="channel-top">
                      <div class="channel-identity">
                        <el-icon :size="20" class="ch-icon"><component :is="ch.icon" /></el-icon>
                        <span class="ch-name">{{ ch.channelName }}</span>
                      </div>
                      <el-switch 
                        v-model="ch.enabled" 
                        :active-value="1" 
                        :inactive-value="0" 
                        active-text="启用" 
                        inactive-text="停用"
                        inline-prompt
                        :disabled="!apiSession || noticeBusy"
                        @change="handleChannelToggle(ch)" 
                      />
                    </div>
                    <div class="channel-desc">{{ ch.remark }}</div>
                    <div class="channel-actions">
                      <el-button link type="primary" size="small" icon="Setting" @click="openChannelConfig(ch)">
                        参数配置
                      </el-button>
                      <el-button link type="success" size="small" icon="Promotion" :disabled="noticeBusy || !apiSession" @click="testChannelPing(ch)">
                        发送测试消息
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 党总支管理员支持发起新党务通知 -->
              <div v-else-if="hasPermission('notice:send')" class="channel-status-cards" style="padding: 12px 20px;">
                <div style="display: flex; align-items: center; justify-content: space-between;">
                  <span style="font-size: 13px; color: #606266;">
                    <el-icon color="#c21c1d"><InfoFilled /></el-icon> 党总支通知发送中枢：支持向三家子公司党支部全体党员及发展对象分发通知与指令。
                  </span>
                  <el-button type="primary" size="small" icon="Promotion" @click="openSendNoticeDialog">
                    发送新党务通知
                  </el-button>
                </div>
              </div>

              <!-- 通知台账管理表格 -->
              <div class="notices-table-card">
                <div class="table-toolbar">
                  <div class="toolbar-left">
                    <el-input 
                      v-model="noticeKeyword" 
                      placeholder="搜索通知标题、内容、接收人..." 
                      prefix-icon="Search"
                      clearable
                      style="width: 260px"
                    />
                    <el-select v-model="filterNoticeType" placeholder="通知类别" clearable style="width: 170px">
                      <el-option label="全部类别" value="" />
                      <el-option label="合规时限预警" value="DEADLINE_WARNING" />
                      <el-option label="转正到期催办" value="TRANS_PROBATION" />
                      <el-option label="纪检把关通知" value="DISCIPLINE_AUDIT" />
                      <el-option label="三会一课通知" value="MEETING_NOTICE" />
                      <el-option label="普通业务通知" value="REGULAR" />
                    </el-select>
                    <el-select v-model="filterNoticeChannel" placeholder="发送渠道" clearable style="width: 160px">
                      <el-option label="全部渠道" value="" />
                      <el-option label="企业微信" value="WECHAT_WORK" />
                      <el-option label="钉钉通知" value="DINGTALK" />
                      <el-option label="阿里云短信" value="SMS" />
                      <el-option label="站内信" value="IN_APP" />
                      <el-option label="电子邮箱" value="EMAIL" />
                    </el-select>
                    <el-select v-model="filterNoticeRead" placeholder="阅读状态" clearable style="width: 130px">
                      <el-option label="全部状态" value="" />
                      <el-option label="未读消息" :value="0" />
                      <el-option label="已读消息" :value="1" />
                    </el-select>
                  </div>
                  <div class="toolbar-right">
                    <el-button icon="Refresh" :loading="noticeBusy" @click="loadNoticeData">刷新记录</el-button>
                    <el-button icon="Check" @click="markAllNoticesRead">本人通知全部已读</el-button>
                  </div>
                </div>

                <el-table :data="filteredNoticeLogs" stripe style="width: 100%" class="custom-dj-table">
                  <el-table-column label="状态" width="80" align="center">
                    <template #default="{ row }">
                      <el-badge v-if="row.isRead === 0" is-dot type="danger">
                        <span class="unread-tag">未读</span>
                      </el-badge>
                      <span v-else class="read-tag">已读</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="通知类别" width="130">
                    <template #default="{ row }">
                      <el-tag :type="row.typeTag" effect="plain" size="small">{{ row.noticeTypeName }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="title" label="通知标题" min-width="220" show-overflow-tooltip>
                    <template #default="{ row }">
                      <span :class="{ 'unread-title': row.isRead === 0 }">{{ row.title }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="content" label="通知正文摘要" min-width="280" show-overflow-tooltip />
                  <el-table-column label="触达渠道" width="120">
                    <template #default="{ row }">
                      <el-tag size="small" type="info">{{ row.channelName }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="receiverName" label="接收对象" width="160" show-overflow-tooltip />
                  <el-table-column label="发送结果" width="145">
                    <template #default="{ row }"><el-tag :type="row.sendStatus === 1 ? 'success' : row.sendStatus === 2 ? 'danger' : 'warning'">{{ noticeStatus(row) }}</el-tag></template>
                  </el-table-column>
                  <el-table-column prop="errorMsg" label="失败原因 / 核对提示" min-width="200" show-overflow-tooltip />
                  <el-table-column prop="sendTime" label="分发时间" width="160" />
                  <el-table-column label="操作" width="120" fixed="right">
                    <template #default="{ row }">
                      <el-button link type="primary" size="small" @click="viewNoticeDetail(row)">详情</el-button>
                      <el-button v-if="row.isRead === 0 && row.receiverId === currentUser.id" link type="success" size="small" @click="markNoticeAsRead(row)">已阅</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </el-tab-pane>

          <!-- 标签页 8: 党务用户与权限角色管理 (仅限系统超级管理员 sys_admin 专享) -->
          <el-tab-pane v-if="hasRole('sys_admin')" name="users">
            <template #label>
              <span class="tab-label"><el-icon><User /></el-icon> 党务用户与权限体系</span>
            </template>

            <div class="users-view-container">
              <!-- 角色权限矩阵卡片 -->
              <div class="roles-summary-card">
                <div class="card-header-row">
                  <div class="title-with-desc">
                    <h3><el-icon><Avatar /></el-icon> 红河智慧党建角色与权限</h3>
                    <span class="sub-tip">严格落实“总支审查把关、支部具体承办、纪检一票否决、党员群众参与”</span>
                  </div>
                  <el-button type="primary" plain icon="Plus" @click="openCreateRoleDialog">新建党务角色</el-button>
                </div>
                <div class="roles-cards-grid">
                  <div v-for="role in sysRoles" :key="role.id" class="role-stat-box">
                    <div class="role-box-top">
                      <span class="role-badge-title">{{ role.roleName }}</span>
                      <el-tag size="small" type="danger" round>{{ role.userCount }} 人在册</el-tag>
                    </div>
                    <p class="role-box-desc">{{ role.description }}</p>
                    <div class="role-perms-chips">
                      <el-tag v-for="p in role.permissions.slice(0, 4)" :key="p" size="small" type="info" class="perm-tag">
                        {{ permissionLabels[p] || '未命名权限' }}
                      </el-tag>
                      <el-tag v-if="role.permissions.length > 4" size="small" type="info" class="perm-tag">
                        +{{ role.permissions.length - 4 }}项
                      </el-tag>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 党务用户列表 -->
              <div class="users-table-card">
                <div class="table-toolbar">
                  <div class="toolbar-left">
                    <el-input 
                      v-model="userKeyword" 
                      placeholder="搜索用户名、姓名、工号、电话..." 
                      prefix-icon="Search"
                      clearable
                      style="width: 250px"
                    />
                    <el-select v-model="filterUserOrg" placeholder="所属党组织" clearable style="width: 240px">
                      <el-option label="全部所属组织" value="" />
                      <el-option label="红数信息支部" value="中共红河红数信息技术服务有限公司支部委员会" />
                      <el-option label="幂次科技支部" value="中共云南幂次科技有限公司支部委员会" />
                      <el-option label="链达科技支部" value="中共红河链达科技有限公司支部委员会" />
                      <el-option label="集团党总支" value="中共红河数据产业集团有限公司总支部委员会" />
                    </el-select>
                    <el-select v-model="filterUserRole" placeholder="分配角色" clearable style="width: 210px">
                      <el-option label="全部角色" value="" />
                      <el-option v-for="r in sysRoles" :key="r.id" :label="r.roleName" :value="r.roleCode" />
                    </el-select>
                  </div>
                  <div class="toolbar-right">
                    <el-button type="primary" icon="Plus" @click="openCreateUserDialog">新建党务用户</el-button>
                  </div>
                </div>

                <el-table :data="filteredSysUsers" stripe style="width: 100%" class="custom-dj-table">
                  <el-table-column label="党员姓名 / 账号" min-width="160">
                    <template #default="{ row }">
                      <div class="user-name-cell">
                        <strong>{{ row.realName }}</strong>
                        <span class="user-acc">@{{ row.username }}</span>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column prop="workNo" label="企业工号" width="130" />
                  <el-table-column prop="orgName" label="所属党组织" min-width="220" show-overflow-tooltip />
                  <el-table-column label="赋予党务角色" min-width="200">
                    <template #default="{ row }">
                      <el-tag type="danger" effect="plain">{{ row.roleName }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="phone" label="联系电话" width="130" />
                  <el-table-column label="账号状态" width="100">
                    <template #default="{ row }">
                      <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                        {{ row.status === 1 ? '正常' : '已停用' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="lastLoginTime" label="最近登录" width="160" />
                  <el-table-column label="操作" width="180" fixed="right">
                    <template #default="{ row }">
                      <el-button link type="primary" size="small" @click="editSysUser(row)">编辑</el-button>
                      <el-button link type="warning" size="small" @click="assignUserRoles(row)">授权</el-button>
                      <el-button 
                        link 
                        :type="row.status === 1 ? 'danger' : 'success'" 
                        size="small" 
                        @click="toggleUserStatus(row)"
                      >
                        {{ row.status === 1 ? '停用' : '启用' }}
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </main>
    </div>

    <!-- ========================================================================= -->
    <!-- 智慧党建数字化大屏模式 (Big Screen Mode: 全屏科技红金态势感知)              -->
    <!-- ========================================================================= -->
    <BigScreenView v-else @close="isBigScreenMode = false" />

    <!-- ========================================================================= -->
    <!-- 模态弹窗区                                                                -->
    <!-- ========================================================================= -->
    <!-- 一人一档全景档案抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      :title="currentMember ? `【一人一档全景电子档案】${currentMember.name}（工号：${currentMember.workNo}）` : '党员档案'"
      size="76%"
      direction="rtl"
      destroy-on-close
      class="member-profile-drawer"
    >
      <div v-if="currentMember" class="drawer-inner-layout">
        <!-- 顶部个人画像摘要卡 -->
        <div class="member-header-card">
          <div class="profile-left">
            <el-avatar :size="54" class="avatar-red-big">{{ currentMember.name.slice(0, 1) }}</el-avatar>
            <div class="profile-text">
              <div class="main-info">
                <h2>{{ currentMember.name }}</h2>
                <el-tag type="danger" effect="dark">{{ currentMember.gender }} / {{ currentMember.age }}岁</el-tag>
                <el-tag type="info">{{ currentMember.education }}</el-tag>
                <el-tag v-if="currentMember.isDualCultivate" type="warning" effect="dark">国企“双培养”骨干</el-tag>
              </div>
              <div class="dept-info">
                <span><el-icon><OfficeBuilding /></el-icon> {{ currentMember.deptName }}</span>
                <span><el-icon><UserFilled /></el-icon> 岗位职务：{{ currentMember.jobTitle }}</span>
                <span><el-icon><Connection /></el-icon> 党组织：{{ currentMember.branchName }}</span>
              </div>
            </div>
          </div>

          <div class="profile-right">
            <div class="cultivator-box">
              <span class="c-label">培养联系人 / 介绍人：</span>
              <strong>{{ currentMember.cultivators ? currentMember.cultivators.join('、') : '暂未指定' }}</strong>
            </div>
            <div class="current-state-box">
              <span class="c-label">当前流转状态：</span>
              <el-tag :type="getStageTagType(currentMember.currentStageId)" size="large" effect="dark">
                第 {{ currentMember.currentStageId }} 阶段 · 第 {{ currentMember.currentStepId }} 步：{{ getStepName(currentMember.currentStepId) }}
              </el-tag>
            </div>
          </div>
        </div>

        <!-- 合规审计告警通知条 -->
        <el-alert
          v-if="currentMember.complianceAlert"
          :title="currentMember.complianceAlert.message"
          :type="currentMember.complianceAlert.type"
          show-icon
          :closable="false"
          class="drawer-compliance-alert"
        />

        <!-- 主体：左侧25步时间轴 + 右侧步骤详情与材料套打 -->
        <div class="drawer-split-body">
          <!-- 左侧：25步流转时钟轴 -->
          <div class="timeline-sidebar">
            <div class="sidebar-header">
              <h4><el-icon><Guide /></el-icon> 25个步骤流转进度树</h4>
              <span class="hint">点击各步骤查看档案材料</span>
            </div>
            <div class="steps-timeline-list">
              <div 
                v-for="step in all25StepsFlat" 
                :key="step.stepId"
                class="timeline-node"
                :class="{
                  'node-finished': step.stepId < currentMember.currentStepId,
                  'node-current': step.stepId === currentMember.currentStepId,
                  'node-future': step.stepId > currentMember.currentStepId,
                  'node-selected': selectedStepInDrawer === step.stepId
                }"
                @click="selectedStepInDrawer = step.stepId"
              >
                <div class="node-icon-col">
                  <span v-if="step.stepId < currentMember.currentStepId" class="icon-circle done">
                    <el-icon><Check /></el-icon>
                  </span>
                  <span v-else-if="step.stepId === currentMember.currentStepId" class="icon-circle current">
                    {{ step.stepId }}
                  </span>
                  <span v-else class="icon-circle future">
                    {{ step.stepId }}
                  </span>
                  <div class="line-segment"></div>
                </div>
                <div class="node-info-col">
                  <div class="node-title">第 {{ step.stepId }} 步：{{ step.name }}</div>
                  <div class="node-meta">
                    <span class="n-role">{{ step.role }}</span>
                    <span v-if="step.stepId === currentMember.currentStepId" class="pulse-tag">正在进行</span>
                    <span v-else-if="step.stepId < currentMember.currentStepId" class="done-tag">已办结</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 右侧：选定步骤材料库与审批操作台 -->
          <div class="step-detail-main">
            <div class="step-main-header">
              <div class="header-tit">
                <h3>第 {{ activeDrawerStepInfo.stepId }} 步：{{ activeDrawerStepInfo.name }}</h3>
                <el-tag size="small" type="danger" effect="plain">{{ activeDrawerStepInfo.role }}</el-tag>
              </div>
              <div class="rule-box">
                <strong>【政策依据与红线】</strong> {{ activeDrawerStepInfo.rule }}
              </div>
            </div>

            <!-- 国企合规审计雷达卡片 -->
            <div class="audit-radar-box" :class="activeDrawerStepAuditClass">
              <div class="radar-title">
                <el-icon><WarningFilled /></el-icon>
                <span>基层党建合规审计防错雷达检测结论</span>
              </div>
              <p class="radar-p">{{ activeDrawerStepAuditDesc }}</p>
            </div>

            <!-- 材料清单与智能套打区 -->
            <div class="materials-section">
              <div class="sec-header">
                <h4><el-icon><FolderChecked /></el-icon> 本步骤归档材料清单与套打</h4>
                <el-button type="primary" size="small" icon="Printer" @click="mockBatchExportDocs">
                  一键套打全套 Word/PDF 材料
                </el-button>
              </div>

              <el-table :data="stepMaterialsList" border style="width: 100%" size="small">
                <el-table-column label="材料名称" min-width="180">
                  <template #default="{ row }">
                    <span class="doc-name"><strong>{{ row.name }}</strong></span>
                  </template>
                </el-table-column>
                <el-table-column label="审核状态" width="120">
                  <template #default="{ row }">
                    <el-tag :type="getMaterialTagType(row.status)">
                      {{ getMaterialStatusText(row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="提交/审核时间" width="140" prop="time" />
                <el-table-column label="操作" width="180">
                  <template #default="{ row }">
                    <el-button link type="primary" size="small" icon="View" @click="previewMaterial(row)">预览</el-button>
                    <el-button link type="success" size="small" icon="Download" @click="downloadSingleDoc(row)">套打导出</el-button>
                    <el-button link type="warning" size="small" icon="Upload">重传</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <!-- 底部推进操作栏 -->
            <div class="step-footer-actions">
              <el-button @click="drawerVisible = false">关闭窗口</el-button>
              <el-button 
                v-if="hasPermission('workbench:advance')"
                type="primary" 
                icon="Check"
                @click="handleAdvanceStep"
              >
                确认审核并推进至下一步
              </el-button>
              <el-tag v-else type="info" effect="plain" style="margin-left: 10px">
                当前角色处于只读查阅模式，无权审批流转
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- 管理员自定义导入模板弹窗 -->
    <el-dialog v-model="uploadDialogVisible" title="管理员自定义导入/替换模板 (.docx)" width="520px">
      <div v-if="selectedTemplateForUpload" class="upload-dialog-body">
        <el-alert
          title="导入说明：上传的企业定制模板将直接应用于本步骤的全套套打。系统将自动解析占位符并生效为当前版本；如格式异常可随时一键恢复系统官方默认模板！"
          type="info"
          :closable="false"
          style="margin-bottom: 16px"
        />
        <el-form label-width="110px">
          <el-form-item label="所属步骤">
            <strong>第 {{ selectedTemplateForUpload.stepId }} 步：{{ selectedTemplateForUpload.name }}</strong>
          </el-form-item>
          <el-form-item label="当前版本">
            <el-tag :type="selectedTemplateForUpload.isCustomized ? 'warning' : 'info'">{{ selectedTemplateForUpload.version }}</el-tag>
          </el-form-item>
          <el-form-item label="选择Word文件">
            <el-upload
              action="#"
              :auto-upload="false"
              :limit="1"
              :on-change="handleFileSelected"
              accept=".docx,.doc"
            >
              <el-button type="primary" icon="Upload">选择 .docx 文件</el-button>
              <template #tip>
                <div class="el-upload__tip">支持 Microsoft Word (.docx) 格式模板，大小不超过 30MB</div>
              </template>
            </el-upload>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" icon="Check" @click="confirmUploadCustomTemplate">确认导入并生效</el-button>
      </template>
    </el-dialog>

    <!-- 模拟套打材料预览弹窗 -->
    <el-dialog v-model="previewDialogVisible" title="党建规范材料预览及一键套打" width="600px">
      <div class="doc-preview-modal-body">
        <div class="doc-paper">
          <div class="doc-paper-header">
            <h3>{{ previewDocTitle }}</h3>
            <span class="doc-code">中国共产党发展党员规范档案 · 编号：HH-DATA-2025-0012</span>
          </div>
          <div class="doc-paper-content">
            <p><strong>被发展对象：</strong> {{ currentMember ? currentMember.name : '' }}（工号：{{ currentMember ? currentMember.workNo : '' }}）</p>
            <p><strong>所属党组织：</strong> {{ currentMember ? currentMember.branchName : '' }}</p>
            <p><strong>工作岗位及职务：</strong> {{ currentMember ? currentMember.jobTitle : '' }}</p>
            <p><strong>审查认定意见：</strong> 该同志政治觉悟高，在红河数据产业科研一线带头攻关、遵纪守法，符合《中国共产党发展党员工作细则》各项要求。</p>
            <div class="sign-seal-box">
              <div class="seal-mark">（党支部印章）</div>
              <div class="sign-line">党支部书记（签名）：李卫民</div>
              <div class="date-line">2025年03月20日</div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="previewDialogVisible = false">关闭</el-button>
        <el-button type="primary" icon="Download" @click="downloadDocSuccess">导出 Word 文档 (.docx)</el-button>
      </template>
    </el-dialog>

    <!-- 录入 / 修改党员信息弹窗 -->
    <el-dialog 
      v-model="addMemberDialogVisible" 
      :title="isEditingMember ? `【修改 / 完善党员档案】${newMemberForm.name || ''}` : '新增录入党员信息（建档入库）'" 
      width="840px"
    >
      <el-form :model="newMemberForm" label-width="140px" class="member-add-form">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="党员姓名" required>
              <el-input v-model="newMemberForm.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="员工工号" required>
              <el-input v-model="newMemberForm.workNo" placeholder="如 HH-HS-088" :disabled="isEditingMember" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="身份证号" required>
              <el-input v-model="newMemberForm.idCard" placeholder="18位公民身份证号" maxlength="18" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别 / 年龄">
              <div style="display: flex; gap: 8px">
                <el-select v-model="newMemberForm.gender" style="width: 100px">
                  <el-option label="男" value="男" />
                  <el-option label="女" value="女" />
                </el-select>
                <el-input-number v-model="newMemberForm.age" :min="18" :max="75" placeholder="年龄" style="flex: 1" />
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="所属党支部" required>
              <!-- 若为支部管理员，强锁本支部且禁用修改，严禁给其他支部录入 -->
              <el-select 
                v-model="newMemberForm.branchName" 
                @change="resetEmployment(newMemberForm)"
                :disabled="isBranchAdmin"
                placeholder="请选择党支部" 
                style="width: 100%"
              >
                <el-option label="红数信息支部" value="中共红河红数信息技术服务有限公司支部委员会" />
                <el-option label="幂次科技支部" value="中共云南幂次科技有限公司支部委员会" />
                <el-option label="链达科技支部" value="中共红河链达科技有限公司支部委员会" />
                <el-option label="集团党总支" value="中共红河数据产业集团有限公司总支部委员会" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最高学历">
              <el-select v-model="newMemberForm.education" placeholder="学历" style="width: 100%">
                <el-option label="大专" value="大专" />
                <el-option label="大学本科" value="大学本科" />
                <el-option label="硕士研究生" value="硕士研究生" />
                <el-option label="博士研究生" value="博士研究生" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="企业行政部门" required>
              <el-select 
                v-model="newMemberForm.deptName" 
                filterable 
                placeholder="请选择行政部门（支持跨公司选择）" 
                style="width: 100%" 
                @change="suggestJobTitle(newMemberForm)"
              >
                <el-option-group 
                  v-for="group in departmentGroups" 
                  :key="group.companyName" 
                  :label="group.companyName"
                >
                  <el-option 
                    v-for="dept in group.options" 
                    :key="dept.value" 
                    :label="dept.value" 
                    :value="dept.value" 
                  />
                </el-option-group>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位职务" required>
              <el-input v-model="newMemberForm.jobTitle" placeholder="请输入岗位职务，可修改推荐岗位" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 跨单位派驻/挂靠智能提示条 -->
        <div v-if="isCrossCompany(newMemberForm.branchName, newMemberForm.deptName)" class="cross-unit-alert">
          <el-icon><InfoFilled /></el-icon>
          <span>
            <strong>【跨单位派驻/挂靠党员】</strong>
            该党员人事编制在<strong>【{{ getCompanyNameFromDept(newMemberForm.deptName) }}】</strong>，
            党组织关系编入<strong>【{{ newMemberForm.branchName }}】</strong>。系统将自动建立跨单位派驻/挂靠档案标记。
          </span>
        </div>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="政治面貌" required>
              <el-select v-model="newMemberForm.partyStatus" placeholder="政治面貌" style="width: 100%">
                <el-option label="正式党员" :value="1" />
                <el-option label="预备党员" :value="2" />
                <el-option label="发展对象" :value="3" />
                <el-option label="入党积极分子" :value="4" />
                <el-option label="入党申请人" :value="5" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="党内职务">
              <el-select v-model="newMemberForm.partyPost" filterable allow-create placeholder="党内职务" style="width: 100%">
                <el-option label="党总支书记" value="党总支书记" />
                <el-option label="支部书记" value="党支部书记" />
                <el-option label="支部副书记" value="支部副书记" />
                <el-option label="组织委员" value="支部组织委员" />
                <el-option label="宣传委员" value="支部宣传委员" />
                <el-option label="纪检委员" value="支部纪检委员" />
                <el-option label="党小组长" value="党小组长" />
                <el-option label="普通党员" value="普通党员" />
                <el-option label="预备党员" value="预备党员" />
                <el-option label="发展对象" value="发展对象" />
                <el-option label="积极分子" value="积极分子" />
                <el-option label="入党申请人" value="入党申请人" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16" v-if="newMemberForm.partyStatus === 1 || newMemberForm.partyStatus === 2">
          <el-col :span="12">
            <el-form-item label="入党时间">
              <el-date-picker v-model="newMemberForm.joinPartyDate" type="date" value-format="YYYY-MM-DD" placeholder="接收预备党员日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="党员党龄折算">
              <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 4px; padding: 6px 12px; font-weight: 700; color: #c21c1d; font-size: 13.5px">
                {{ calculatePartyStandingYears(newMemberForm.joinPartyDate) }} 年
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="年度集中培训时长">
              <el-input-number v-model="newMemberForm.studyHours" :min="0" :max="120" style="width: 100%" placeholder="目标40学时" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="正式转正日期">
              <el-date-picker v-model="newMemberForm.officialPartyDate" type="date" value-format="YYYY-MM-DD" placeholder="正式党员转正日期" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="roster-filing-tip-box" style="margin-bottom: 16px; padding: 10px 14px; background: #fffbeb; border: 1px dashed #f59e0b; border-radius: 6px; font-size: 12px; color: #b45309; display: flex; align-items: center; gap: 8px;">
          <el-icon><InfoFilled /></el-icon>
          <span><strong>规范提示：</strong>党员组织关系转入、转出（除名）以及党内职务任免，请前往【<strong>党员转接及调整备案</strong>】模块规范办理，系统将自动联动花名册增减与职务变更。</span>
        </div>

        <el-form-item label="国企骨干标签">
          <div style="display: flex; flex-wrap: wrap; gap: 18px; align-items: center; width: 100%;">
            <el-checkbox v-model="newMemberForm.isFrontline">生产/业务一线骨干</el-checkbox>
            <el-checkbox v-model="newMemberForm.isTechnicalTalent">数字研发核心技术骨干</el-checkbox>
            <el-checkbox v-model="newMemberForm.isDualCultivate">列入“双培养”工程</el-checkbox>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addMemberDialogVisible = false">取消</el-button>
        <el-button type="primary" icon="Check" @click="submitAddMember">
          {{ isEditingMember ? '保存修改档案' : '确认新增入库' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 批量导入党员花名册弹窗 -->
    <el-dialog v-model="importRosterDialogVisible" title="批量导入党员花名册 (Excel .xlsx)" width="680px">
      <div class="roster-import-dialog-body">
        <el-steps :active="importActiveStep" finish-status="success" simple style="margin-bottom: 20px">
          <el-step title="1.下载模板" icon="Download" />
          <el-step title="2.上传文件" icon="Upload" />
          <el-step title="3.校验入库" icon="Check" />
        </el-steps>

        <!-- 步骤1：下载模板 -->
        <div class="import-step-card">
          <div class="step-card-tit"><strong>第一步：下载标准导入模板</strong></div>
          <p class="step-card-desc">请先下载规范的 Excel 导入模板，按照格式填写现有党员及入党积极分子等信息（包含姓名、工号、身份证号、支部、政治面貌等）。</p>
          <el-button type="primary" icon="Download" @click="downloadRosterTemplateExcel">
            下载党员花名册导入模板 (.xlsx)
          </el-button>
        </div>

        <!-- 步骤2：上传文件 -->
        <div class="import-step-card" style="margin-top: 14px">
          <div class="step-card-tit"><strong>第二步：选择填好的 Excel 文件</strong></div>
          <el-upload
            action="#"
            :auto-upload="false"
            :limit="1"
            accept=".xlsx,.xls"
            :on-change="handleRosterExcelSelected"
          >
            <el-button type="warning" icon="Document">选择已填写的 Excel 文件</el-button>
            <template #tip>
              <div class="el-upload__tip">支持 .xlsx / .xls 格式，每次导入建议不超过 200 条记录</div>
            </template>
          </el-upload>
        </div>

        <!-- 步骤3：解析预览与合规检测 -->
        <div v-if="parsedRosterPreviewList.length > 0" class="import-preview-box" style="margin-top: 14px">
          <div class="preview-header">
            <el-tag type="success">
              <el-icon><Check /></el-icon> 成功解析 {{ parsedRosterPreviewList.length }} 条党员数据（无工号与身份证冲突）
            </el-tag>
          </div>
          <el-table :data="parsedRosterPreviewList" size="small" border max-height="180" style="margin-top: 8px">
            <el-table-column prop="name" label="姓名" width="90" />
            <el-table-column prop="workNo" label="工号" width="110" />
            <el-table-column prop="branchName" label="所属党组织" min-width="160" show-overflow-tooltip />
            <el-table-column prop="partyPost" label="职务" width="100" />
            <el-table-column label="政治面貌" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="getRosterPartyStatusTag(row.partyStatus)">{{ getRosterPartyStatusText(row.partyStatus) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <template #footer>
        <el-button @click="importRosterDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          icon="Check" 
          :disabled="parsedRosterPreviewList.length === 0" 
          @click="confirmBatchImportRoster"
        >
          确认批量导入并入库 ({{ parsedRosterPreviewList.length }}人)
        </el-button>
      </template>
    </el-dialog>

    <!-- ============================================== -->
    <!-- 弹窗 1: 办理组织关系转入弹窗 -->
    <!-- ============================================== -->
    <el-dialog 
      v-model="transferInDialogVisible" 
      title="办理党员组织关系转入 (自动同步建档至花名册)" 
      width="820px"
    >
      <el-form :model="transferInForm" label-width="140px" class="member-add-form">
        <el-alert 
          type="success" 
          :closable="false" 
          show-icon 
          style="margin-bottom: 18px"
        >
          <template #title>
            <strong>联动提示：</strong>转入手续办结后，系统将自动把该党员档案同步写入【党员花名册】，无需重复录入。
          </template>
        </el-alert>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="党员姓名" required>
              <el-input v-model="transferInForm.memberName" placeholder="请输入转入党员姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="员工工号" required>
              <el-input v-model="transferInForm.workNo" placeholder="如 HH-HS-099" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="身份证号" required>
              <el-input v-model="transferInForm.idCard" placeholder="18位公民身份证号码" maxlength="18" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别/政治面貌" required>
              <div style="display: flex; gap: 8px; width: 100%">
                <el-select v-model="transferInForm.gender" style="width: 80px">
                  <el-option label="男" value="男" />
                  <el-option label="女" value="女" />
                </el-select>
                <el-select v-model="transferInForm.partyStatus" style="flex: 1">
                  <el-option label="正式党员" :value="1" />
                  <el-option label="预备党员" :value="2" />
                </el-select>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="原所在党组织" required>
              <el-input v-model="transferInForm.fromOrgName" placeholder="如 中共云南省电子信息检验院支部" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="拟转入党支部" required>
              <el-select 
                v-model="transferInForm.toOrgName" 
                :disabled="isBranchAdmin"
                placeholder="请选择接收党支部" 
                style="width: 100%"
                @change="suggestTransferInEmployment"
              >
                <el-option label="红数信息支部" value="中共红河红数信息技术服务有限公司支部委员会" />
                <el-option label="幂次科技支部" value="中共云南幂次科技有限公司支部委员会" />
                <el-option label="链达科技支部" value="中共红河链达科技有限公司支部委员会" />
                <el-option label="集团党总支" value="中共红河数据产业集团有限公司总支部委员会" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="企业行政部门" required>
              <el-select v-model="transferInForm.deptName" placeholder="请选择部门" style="width: 100%">
                <el-option v-for="dept in getDepartmentOptions(transferInForm.toOrgName)" :key="dept.value" :label="dept.label" :value="dept.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位/党内职务">
              <div style="display: flex; gap: 8px">
                <el-input v-model="transferInForm.jobTitle" placeholder="行政职务" style="flex: 1" />
                <el-select v-model="transferInForm.partyPost" style="width: 120px">
                  <el-option label="普通党员" value="普通党员" />
                  <el-option label="支部组织委员" value="支部组织委员" />
                  <el-option label="支部宣传委员" value="支部宣传委员" />
                  <el-option label="支部纪检委员" value="支部纪检委员" />
                  <el-option label="党小组长" value="党小组长" />
                </el-select>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="介绍信文号" required>
              <el-input v-model="transferInForm.letterNo" placeholder="如 云信转字〔2026〕第01号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="转接入库日期" required>
              <el-date-picker v-model="transferInForm.transferDate" type="date" value-format="YYYY-MM-DD" placeholder="办理转接日期" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="党费交至年月">
              <el-date-picker v-model="transferInForm.duesPaidToDate" type="month" value-format="YYYY-MM" placeholder="已在原单位交至月份" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="transferInForm.phone" placeholder="手机号码" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="转入事由备注">
          <el-input v-model="transferInForm.transferReason" placeholder="如 业务骨干高层次人才引进调入，已审核入党志愿书档案合格" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="transferInDialogVisible = false">取消</el-button>
        <el-button type="success" icon="Check" @click="submitTransferIn">
          确认接收并同步至花名册
        </el-button>
      </template>
    </el-dialog>

    <!-- ============================================== -->
    <!-- 弹窗 2: 办理组织关系转出弹窗 -->
    <!-- ============================================== -->
    <el-dialog 
      v-model="transferOutDialogVisible" 
      title="办理党员组织关系转出 (自动从花名册除名注销)" 
      width="780px"
    >
      <el-form :model="transferOutForm" label-width="140px" class="member-add-form">
        <el-alert 
          type="error" 
          :closable="false" 
          show-icon 
          style="margin-bottom: 18px"
        >
          <template #title>
            <strong>除名警示：</strong>转出手续办结后，该党员将自动从【党员花名册】中除名注销，流转至转接历史归档，请审慎核实！
          </template>
        </el-alert>

        <el-form-item label="选择转出党员" required>
          <el-select 
            v-model="transferOutForm.selectedWorkNo" 
            filterable 
            placeholder="请搜索或选择在册党员姓名 / 工号" 
            style="width: 100%"
            @change="handleSelectTransferOutMember"
          >
            <el-option 
              v-for="m in rosterList" 
              :key="m.workNo" 
              :label="`${m.name} (${m.workNo}) · ${m.branchName} · ${m.partyPost || '普通党员'}`" 
              :value="m.workNo" 
            />
          </el-select>
        </el-form-item>

        <el-row :gutter="16" v-if="transferOutForm.selectedWorkNo">
          <el-col :span="12">
            <el-form-item label="党员姓名/工号">
              <el-input :model-value="`${transferOutForm.memberName} (${transferOutForm.workNo})`" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="现所在党支部">
              <el-input :model-value="transferOutForm.fromOrgName" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="拟转往党组织" required>
              <el-input v-model="transferOutForm.toOrgName" placeholder="如 中共红河州开发区建设投资党支部 / 中共XX局机关党委" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="介绍信编号" required>
              <el-input v-model="transferOutForm.letterNo" placeholder="如 红数转字〔2026〕第05号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="转出除名日期" required>
              <el-date-picker v-model="transferOutForm.transferDate" type="date" value-format="YYYY-MM-DD" placeholder="转出日期" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="党费交至年月">
              <el-date-picker v-model="transferOutForm.duesPaidToDate" type="month" value-format="YYYY-MM" placeholder="党费交至当前月" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经办组织员">
              <el-input v-model="transferOutForm.operatorName" placeholder="经办人姓名" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="转出除名原因" required>
          <el-input 
            v-model="transferOutForm.transferReason" 
            type="textarea" 
            :rows="2" 
            placeholder="因个人工作调动离职除名 / 退休迁出居住地党支部 / 外调上级单位" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="transferOutDialogVisible = false">取消</el-button>
        <el-button type="danger" icon="Right" @click="submitTransferOut">
          确认转出并从花名册除名
        </el-button>
      </template>
    </el-dialog>

    <!-- ============================================== -->
    <!-- 弹窗 3: 新增党员党内职务调整备案弹窗 -->
    <!-- ============================================== -->
    <el-dialog 
      v-model="adjustmentDialogVisible" 
      title="录入党员党内职务调整备案 (自动联动花名册党内职务)" 
      width="780px"
    >
      <el-form :model="adjustmentForm" label-width="140px" class="member-add-form">
        <el-alert 
          type="primary" 
          :closable="false" 
          show-icon 
          style="margin-bottom: 18px"
        >
          <template #title>
            <strong>职务联动：</strong>备案录入审核后，系统将自动把新职务同步至花名册中对应党员的“党内职务”，并保留批文号永久备查。
          </template>
        </el-alert>

        <el-form-item label="选择任职党员" required>
          <el-select 
            v-model="adjustmentForm.selectedWorkNo" 
            filterable 
            placeholder="请搜索或选择在册党员姓名 / 工号" 
            style="width: 100%"
            @change="handleSelectAdjustmentMember"
          >
            <el-option 
              v-for="m in rosterList" 
              :key="m.workNo" 
              :label="`${m.name} (${m.workNo}) · ${m.branchName} · 现职务：${m.partyPost || '普通党员'}`" 
              :value="m.workNo" 
            />
          </el-select>
        </el-form-item>

        <el-row :gutter="16" v-if="adjustmentForm.selectedWorkNo">
          <el-col :span="12">
            <el-form-item label="任职党支部">
              <el-input :model-value="adjustmentForm.orgName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="调整前原职务">
              <el-input :model-value="adjustmentForm.oldPost || '普通党员'" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="新任党内职务" required>
              <el-select v-model="adjustmentForm.newPost" placeholder="请选择新任职务" style="width: 100%">
                <el-option label="党总支书记" value="党总支书记" />
                <el-option label="党总支副书记" value="党总支副书记" />
                <el-option label="党支部书记" value="党支部书记" />
                <el-option label="支部副书记" value="支部副书记" />
                <el-option label="支部组织委员" value="支部组织委员" />
                <el-option label="支部宣传委员" value="支部宣传委员" />
                <el-option label="支部纪检委员" value="支部纪检委员" />
                <el-option label="党小组长" value="党小组长" />
                <el-option label="普通党员 (免职回任)" value="普通党员" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="调整备案类型" required>
              <el-select v-model="adjustmentForm.adjustType" placeholder="调整类型" style="width: 100%">
                <el-option label="任职任命" value="任职任命" />
                <el-option label="支委会选举" value="支委会选举" />
                <el-option label="分工微调" value="分工微调" />
                <el-option label="免去职务" value="免去职务" />
                <el-option label="届满换届" value="届满换届" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="批准文号编号" required>
              <el-input v-model="adjustmentForm.documentNo" placeholder="如 红数党总任〔2026〕第03号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任职生效日期" required>
              <el-date-picker v-model="adjustmentForm.effectiveDate" type="date" value-format="YYYY-MM-DD" placeholder="生效日期" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="批准决定单位" required>
          <el-input v-model="adjustmentForm.approvalUnit" placeholder="如 中共红河数据产业集团有限公司总支部委员会" />
        </el-form-item>

        <el-form-item label="主要职责分工">
          <el-input 
            v-model="adjustmentForm.dutyDescription" 
            type="textarea" 
            :rows="2" 
            placeholder="如 主持党支部全面工作，兼任党支部纪律检查委员职责，强化国企基层党风廉政建设" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustmentDialogVisible = false">取消</el-button>
        <el-button type="primary" icon="Check" @click="submitAdjustment">
          确认备案并更新花名册职务
        </el-button>
      </template>
    </el-dialog>

    <!-- ============================================== -->
    <!-- 弹窗 4: 组织关系转接介绍信详情凭证弹窗 -->
    <!-- ============================================== -->
    <el-dialog 
      v-model="viewTransferDialogVisible" 
      title="中国共产党党员组织关系介绍信存根与凭证" 
      width="680px"
    >
      <div v-if="currentTransferView" class="transfer-cert-box">
        <div class="cert-header">
          <div class="cert-badge">中共党内凭证存根</div>
          <h3 class="cert-title">中国共产党党员组织关系转接存根凭证</h3>
          <div class="cert-no">批复编号：{{ currentTransferView.letterNo }}</div>
        </div>

        <div class="cert-divider"></div>

        <div class="cert-grid">
          <div class="cert-row"><span class="cert-lbl">党员姓名：</span><span class="cert-val font-bold">{{ currentTransferView.memberName }}</span></div>
          <div class="cert-row"><span class="cert-lbl">员工工号：</span><span class="cert-val">{{ currentTransferView.workNo }}</span></div>
          <div class="cert-row"><span class="cert-lbl">身份证号：</span><span class="cert-val">{{ currentTransferView.idCard }}</span></div>
          <div class="cert-row"><span class="cert-lbl">政治面貌：</span><span class="cert-val">{{ currentTransferView.partyStatus === 1 ? '中国共产党正式党员' : '中国共产党预备党员' }}</span></div>
          <div class="cert-row"><span class="cert-lbl">转接方向：</span>
            <el-tag :type="currentTransferView.transferType === 1 ? 'success' : 'danger'" effect="dark" size="small">
              {{ currentTransferView.transferType === 1 ? '组织关系转入' : '组织关系转出' }}
            </el-tag>
          </div>
          <div class="cert-row"><span class="cert-lbl">转出党组织：</span><span class="cert-val text-red">{{ currentTransferView.fromOrgName }}</span></div>
          <div class="cert-row"><span class="cert-lbl">接收党组织：</span><span class="cert-val text-blue font-bold">{{ currentTransferView.toOrgName }}</span></div>
          <div class="cert-row"><span class="cert-lbl">转接生效日期：</span><span class="cert-val">{{ currentTransferView.transferDate }}</span></div>
          <div class="cert-row"><span class="cert-lbl">党费交纳至：</span><span class="cert-val">{{ currentTransferView.duesPaidToDate || '当月已结清' }}</span></div>
          <div class="cert-row"><span class="cert-lbl">转接原因说明：</span><span class="cert-val">{{ currentTransferView.transferReason }}</span></div>
          <div class="cert-row"><span class="cert-lbl">经办人：</span><span class="cert-val">{{ currentTransferView.operatorName }}</span></div>
          <div class="cert-row"><span class="cert-lbl">备注说明：</span><span class="cert-val">{{ currentTransferView.remark || '档案核查完整无误，已归档' }}</span></div>
        </div>

        <div class="cert-seal-box">
          <div class="seal-party-text">中共红河数据产业集团有限公司总支部委员会</div>
          <div class="seal-date-text">{{ currentTransferView.transferDate }}</div>
          <div class="cert-red-stamp">组织关系<br>专用章</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="viewTransferDialogVisible = false">关闭</el-button>
        <el-button type="primary" icon="Printer" @click="printTransferCert">打印介绍信凭证</el-button>
      </template>
    </el-dialog>

    <!-- ============================================== -->
    <!-- 弹窗 5: 干部党内职务调整任免备案登记表详情弹窗 -->
    <!-- ============================================== -->
    <el-dialog 
      v-model="viewAdjustmentDialogVisible" 
      title="党员领导干部党内职务调整任免备案表" 
      width="680px"
    >
      <div v-if="currentAdjustmentView" class="transfer-cert-box">
        <div class="cert-header">
          <div class="cert-badge bg-gold">干部党内任免</div>
          <h3 class="cert-title">党内职务调整备案登记卡</h3>
          <div class="cert-no">正式批文号：{{ currentAdjustmentView.documentNo }}</div>
        </div>

        <div class="cert-divider"></div>

        <div class="cert-grid">
          <div class="cert-row"><span class="cert-lbl">党员姓名：</span><span class="cert-val font-bold">{{ currentAdjustmentView.memberName }}</span></div>
          <div class="cert-row"><span class="cert-lbl">员工工号：</span><span class="cert-val">{{ currentAdjustmentView.workNo }}</span></div>
          <div class="cert-row"><span class="cert-lbl">任职党组织：</span><span class="cert-val font-bold">{{ currentAdjustmentView.orgName }}</span></div>
          <div class="cert-row"><span class="cert-lbl">原党内职务：</span><span class="cert-val text-muted">{{ currentAdjustmentView.oldPost || '普通党员' }}</span></div>
          <div class="cert-row"><span class="cert-lbl">新任党内职务：</span>
            <el-tag type="danger" effect="dark" size="small" style="font-weight: 700">
              {{ currentAdjustmentView.newPost }}
            </el-tag>
          </div>
          <div class="cert-row"><span class="cert-lbl">调整类别：</span><span class="cert-val">{{ currentAdjustmentView.adjustType }}</span></div>
          <div class="cert-row"><span class="cert-lbl">批准机关：</span><span class="cert-val text-red font-bold">{{ currentAdjustmentView.approvalUnit }}</span></div>
          <div class="cert-row"><span class="cert-lbl">任职生效日期：</span><span class="cert-val">{{ currentAdjustmentView.effectiveDate }}</span></div>
          <div class="cert-row full-width"><span class="cert-lbl">主要职责分工：</span><span class="cert-val">{{ currentAdjustmentView.dutyDescription || '主持支部班子全面工作，抓好思想政治与党风廉政建设' }}</span></div>
          <div class="cert-row"><span class="cert-lbl">备案经办人：</span><span class="cert-val">{{ currentAdjustmentView.operatorName }}</span></div>
          <div class="cert-row"><span class="cert-lbl">名册联动状态：</span><span class="cert-val text-green font-bold"><el-icon><Check /></el-icon> 已同步花名册</span></div>
        </div>

        <div class="cert-seal-box">
          <div class="seal-party-text">{{ currentAdjustmentView.approvalUnit }}</div>
          <div class="seal-date-text">{{ currentAdjustmentView.effectiveDate }}</div>
          <div class="cert-red-stamp">党内职务<br>备案章</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="viewAdjustmentDialogVisible = false">关闭</el-button>
        <el-button type="primary" icon="Printer" @click="printAdjustmentCert">打印任免备案卡</el-button>
      </template>
    </el-dialog>

    <!-- 记录/修改组织生活会议弹窗 -->
    <el-dialog 
      v-model="addMeetingDialogVisible" 
      :title="isEditingMeeting ? `【修改 / 完善会议记录】${newMeetingForm.title || ''}` : '发起 / 记录支部“三会一课”与主题党日'" 
      width="760px"
    >
      <el-form :model="newMeetingForm" label-width="135px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="所属党支部" required>
              <!-- 若为支部管理员，强锁本支部且禁用修改，严禁给其他支部代录组织生活 -->
              <el-select 
                v-model="newMeetingForm.branchName" 
                :disabled="isBranchAdmin"
                style="width: 100%"
              >
                <el-option label="红数信息支部 (云服务/安全)" value="中共红河红数信息技术服务有限公司支部委员会" />
                <el-option label="幂次科技支部 (软件开发/数字化)" value="中共云南幂次科技有限公司支部委员会" />
                <el-option label="链达科技支部 (城市综合体运营)" value="中共红河链达科技有限公司支部委员会" />
                <el-option label="集团党总支" value="中共红河数据产业集团有限公司总支部委员会" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="组织生活类型" required>
              <el-select v-model="newMeetingForm.meetingType" style="width: 100%">
                <el-option label="支委会 (每月至少1次)" :value="1" />
                <el-option label="支部党员大会 (每季度1次)" :value="2" />
                <el-option label="专题党课 (每季度1次)" :value="3" />
                <el-option label="每月主题党日" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="会议主要议题" required>
          <el-input v-model="newMeetingForm.title" placeholder="如 讨论接收王建国同志为预备党员 / 筑牢网络安全底座主题党日" />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="召开日期" required>
              <el-date-picker v-model="newMeetingForm.date" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开会地点">
              <el-input v-model="newMeetingForm.place" placeholder="如 党员活动室 / 综合体现场" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="主持人" required>
              <el-input v-model="newMeetingForm.moderator" placeholder="如 李卫民 (支部书记)" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="主讲人(党课)">
              <el-input v-model="newMeetingForm.speaker" placeholder="讲党课时填写" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="应到人数">
              <el-input-number v-model="newMeetingForm.expectedCount" :min="1" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实到人数">
              <el-input-number v-model="newMeetingForm.actualCount" :min="1" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="发展党员联动">
              <el-select v-model="newMeetingForm.relatedStep" placeholder="是否关联发展党员" style="width: 100%">
                <el-option label="常规组织生活 (无关联)" :value="0" />
                <el-option label="第4步: 支委会确定积极分子" :value="4" />
                <el-option label="第7步: 积极分子季度考察写实" :value="7" />
                <el-option label="第10步: 支委会确定发展对象" :value="10" />
                <el-option label="第18步: 支部大会讨论接收预备党员" :value="18" />
                <el-option label="第24步: 支部大会讨论预备党员转正" :value="24" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="newMeetingForm.relatedStep > 0">
            <el-form-item label="关联成员姓名">
              <el-input v-model="newMeetingForm.relatedMember" placeholder="被讨论党员姓名" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 会议主要研究议题 (支持动态添加文本框与彩色标签) -->
        <div class="agenda-items-section" style="margin-bottom: 16px; background: #fdfaf2; border: 1px solid #faecd8; border-radius: 6px; padding: 12px 14px">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px">
            <span style="font-weight: 700; font-size: 13px; color: #b88230">
              <el-icon><Tickets /></el-icon> 会议重点研究议题及分类标签 (支持动态增加)
            </span>
            <div style="display: flex; gap: 8px">
              <el-button size="small" type="primary" plain icon="Plus" @click="addAgendaItemRow">
                增加一项议题
              </el-button>
              <el-button size="small" type="warning" plain icon="CollectionTag" @click="openManageTagsDialog">
                管理标签
              </el-button>
            </div>
          </div>

          <div v-if="newMeetingForm.agendaItems.length === 0" style="font-size: 12px; color: #909399; text-align: center; padding: 10px 0">
            暂未添加重点研究议题，点击上方“增加一项议题”开始录入
          </div>

          <div v-else style="display: flex; flex-direction: column; gap: 8px">
            <div 
              v-for="(agenda, aIdx) in newMeetingForm.agendaItems" 
              :key="aIdx"
              style="display: flex; align-items: center; gap: 8px"
            >
              <span style="font-size: 12px; color: #64748b; white-space: nowrap">议题{{ aIdx + 1 }}:</span>
              <el-input 
                v-model="agenda.topic" 
                placeholder="请输入会议重点研究的议题事项..." 
                style="flex: 1" 
              />
              <el-select 
                v-model="agenda.tagId" 
                placeholder="选择标签" 
                style="width: 150px"
                @change="(val) => handleAgendaTagChange(agenda, val)"
              >
                <el-option 
                  v-for="tag in availableTopicTags" 
                  :key="tag.id" 
                  :label="tag.name" 
                  :value="tag.id"
                >
                  <div style="display: flex; align-items: center; gap: 6px">
                    <span :style="{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: tag.color }"></span>
                    <span>{{ tag.name }}</span>
                  </div>
                </el-option>
              </el-select>
              <el-tag v-if="agenda.tagName" :color="agenda.tagColor" effect="dark" style="border: none; color: #fff; font-size: 11px">
                {{ agenda.tagName }}
              </el-tag>
              <el-button type="danger" link icon="Delete" @click="removeAgendaItemRow(aIdx)"></el-button>
            </div>
          </div>
        </div>

        <el-form-item label="主要决议纪实">
          <el-input v-model="newMeetingForm.content" type="textarea" :rows="3" placeholder="简要记录会议讨论过程、投票结果与决议内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addMeetingDialogVisible = false">取消</el-button>
        <el-button type="primary" icon="Check" @click="submitAddMeeting">
          {{ isEditingMeeting ? '保存修改内容' : '保存会议台账' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 议题分类标签管理与管理员新增弹窗 (彩色标记) -->
    <el-dialog v-model="manageTagsDialogVisible" title="议题分类标签管理 (支持管理员自定义新增与颜色标记)" width="560px">
      <div style="display: flex; flex-direction: column; gap: 14px">
        <!-- 新增标签区 -->
        <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 6px; padding: 12px 14px">
          <div style="font-weight: 700; font-size: 13px; color: #1e293b; margin-bottom: 8px">
            <el-icon><Plus /></el-icon> 新增自定义议题分类标签
          </div>
          <div style="display: flex; gap: 8px; align-items: center">
            <el-input v-model="newTagName" placeholder="输入标签名称，如 发展党员/党纪学习" style="flex: 1" />
            <el-color-picker v-model="newTagColor" :predefine="['#c21c1d', '#e6a23c', '#67c23a', '#409eff', '#8b5cf6', '#ec4899', '#f97316', '#06b6d4']" />
            <el-button type="primary" icon="Check" @click="submitAddNewTag">添加标签</el-button>
          </div>
        </div>

        <!-- 现有标签库 -->
        <div style="font-weight: 700; font-size: 13px; color: #334155">
          当前可用议题分类标签库 (共 {{ availableTopicTags.length }} 个)
        </div>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px; max-height: 240px; overflow-y: auto">
          <div 
            v-for="tag in availableTopicTags" 
            :key="tag.id"
            style="display: flex; justify-content: space-between; align-items: center; background: #fff; border: 1px solid #e2e8f0; border-radius: 4px; padding: 8px 12px"
          >
            <div style="display: flex; align-items: center; gap: 8px">
              <span :style="{ width: '10px', height: '10px', borderRadius: '50%', backgroundColor: tag.color }"></span>
              <el-tag :color="tag.color" effect="dark" style="border: none; color: #fff; font-size: 11px">
                {{ tag.name }}
              </el-tag>
            </div>
            <el-button type="danger" link icon="Close" size="small" @click="deleteTopicTag(tag.id)"></el-button>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="manageTagsDialogVisible = false">完成</el-button>
      </template>
    </el-dialog>

    <!-- 三会一课全套规范附件管理弹窗 (通知、纪要、决议、签到表、现场照片) -->
    <el-dialog 
      v-model="meetingAttachmentsDialogVisible" 
      :title="currentMeetingForAttach ? `【会议规范附件归档】${currentMeetingForAttach.title}` : '会议附件管理'" 
      width="720px"
    >
      <div v-if="currentMeetingForAttach" class="meeting-attach-dialog-body">
        <el-alert
          title="归档规范：党组织召开支委会、党员大会、党课与主题党日必须完整留存【会议通知】、【会议纪要】、【决议书】与【签到表原件扫描件】，以备迎检巡视巡察合规抽查。"
          type="info"
          :closable="false"
          style="margin-bottom: 14px"
        />

        <!-- 上传新附件区 -->
        <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 6px; padding: 12px 16px; margin-bottom: 16px">
          <div style="font-weight: 700; font-size: 13px; color: #303133; margin-bottom: 8px">
            <el-icon><Upload /></el-icon> 上传新增会议附件材料
          </div>
          <div style="display: flex; gap: 10px; align-items: center; flex-wrap: wrap">
            <el-select v-model="uploadMeetingAttachType" placeholder="选择附件类别" style="width: 170px">
              <el-option label="会议通知" value="NOTICE" />
              <el-option label="会议纪要" value="MINUTES" />
              <el-option label="表决决议书" value="RESOLUTION" />
              <el-option label="签到考勤表" value="SIGNIN" />
              <el-option label="现场纪实照片" value="PHOTO" />
            </el-select>
            <el-upload
              action="#"
              :auto-upload="false"
              :limit="1"
              accept=".docx,.doc,.pdf,.jpg,.png"
              :on-change="handleMeetingAttachSelected"
            >
              <el-button icon="Folder">选择本地文件</el-button>
            </el-upload>
            <el-button type="primary" icon="Check" @click="confirmUploadMeetingAttach">确认上传归档</el-button>
          </div>
        </div>

        <!-- 已归档附件列表表格 -->
        <div style="font-weight: 700; font-size: 13.5px; color: #2c3e50; margin-bottom: 8px">
          已归档附件档案清单 ({{ currentMeetingAttachmentsList.length }} 件)
        </div>
        <el-table :data="currentMeetingAttachmentsList" border size="small" style="width: 100%">
          <el-table-column label="附件类别" width="130">
            <template #default="{ row }">
              <el-tag :type="getAttachCategoryTag(row.attachType)" size="small">
                {{ row.attachTypeName }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="文件名称" min-width="220" prop="fileName" show-overflow-tooltip />
          <el-table-column label="文件大小" width="100" prop="fileSize" />
          <el-table-column label="上传时间" width="140" prop="uploadTime" />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" icon="Download" @click="downloadSingleMeetingAttach(row)">
                下载
              </el-button>
              <el-button link type="danger" size="small" icon="Delete" @click="deleteMeetingAttach(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button type="primary" @click="meetingAttachmentsDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 登记/修改组织与个人奖惩荣誉弹窗 -->
    <el-dialog 
      v-model="honorDialogVisible" 
      :title="isEditingHonor ? `【修改奖惩/荣誉记录】${newHonorForm.title || ''}` : '登记组织或个人奖惩/荣誉'" 
      width="760px"
    >
      <el-form :model="newHonorForm" label-width="135px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="奖惩主体分类" required>
              <el-radio-group v-model="newHonorForm.category" @change="handleHonorCategoryChange">
                <el-radio :label="2">组织奖惩/荣誉</el-radio>
                <el-radio :label="1">个人奖惩/荣誉</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="奖惩性质" required>
              <el-radio-group v-model="newHonorForm.recordType">
                <el-radio :label="1">荣誉表彰</el-radio>
                <el-radio :label="2">纪律处分/诫勉</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="奖惩/表彰名称" required>
          <el-input v-model="newHonorForm.title" placeholder="如 云南省国资委先进基层党组织 / 云南省数字技术工匠" />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="newHonorForm.category === 2 ? '获奖党支部' : '党员姓名'" required>
              <!-- 组织荣誉：若为支部管理员，直接强锁本支部且禁用修改，严禁给其他支部申报组织表彰/处分 -->
              <el-select 
                v-if="newHonorForm.category === 2" 
                v-model="newHonorForm.targetName" 
                :disabled="isBranchAdmin"
                placeholder="选择党支部" 
                style="width: 100%"
                @change="handleOrgTargetChange"
              >
                <el-option label="红数信息党支部" value="中共红河红数信息技术服务有限公司支部委员会" />
                <el-option label="幂次科技党支部" value="中共云南幂次科技有限公司支部委员会" />
                <el-option label="链达科技党支部" value="中共红河链达科技有限公司支部委员会" />
                <el-option label="集团党总支" value="中共红河数据产业集团有限公司总支部委员会" />
              </el-select>
              <!-- 个人荣誉：若为支部管理员，下拉名单仅呈现本支部党员，严禁给其他支部的党员录入荣誉/处分 -->
              <el-select 
                v-else 
                v-model="newHonorForm.targetName" 
                filterable 
                placeholder="输入或选择党员" 
                style="width: 100%"
                @change="handleMemberTargetChange"
              >
                <el-option 
                  v-for="m in branchLockedMemberRoster" 
                  :key="m.id" 
                  :label="`${m.name} (${m.workNo} - ${m.branchName.slice(0, 10)}...)`" 
                  :value="m.name" 
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属党组织" required>
              <!-- 若为支部管理员，强锁本支部且禁用修改 -->
              <el-select 
                v-model="newHonorForm.orgName" 
                :disabled="isBranchAdmin"
                placeholder="所属党组织" 
                style="width: 100%"
              >
                <el-option label="红数信息支部" value="中共红河红数信息技术服务有限公司支部委员会" />
                <el-option label="幂次科技支部" value="中共云南幂次科技有限公司支部委员会" />
                <el-option label="链达科技支部" value="中共红河链达科技有限公司支部委员会" />
                <el-option label="集团党总支" value="中共红河数据产业集团有限公司总支部委员会" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="表彰/处分级别" required>
              <el-select v-model="newHonorForm.level" placeholder="请选择级别" style="width: 100%">
                <el-option label="国家级" value="国家级" />
                <el-option label="省部级" value="省部级" />
                <el-option label="州级/市级" value="州级/市级" />
                <el-option label="集团级" value="集团级" />
                <el-option label="支部级" value="支部级" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="决定日期" required>
              <el-date-picker v-model="newHonorForm.recordDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="授予/决定单位">
              <el-input v-model="newHonorForm.grantOrg" placeholder="如 云南省国资委党委 / 集团党总支" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="红头/处分文号">
              <el-input v-model="newHonorForm.docNo" placeholder="如 云国资党委发〔2024〕18号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item :label="newHonorForm.recordType === 1 ? '主要表彰事迹' : '处分事实与原因'">
          <el-input v-model="newHonorForm.reasonContent" type="textarea" :rows="3" placeholder="详细填写事迹说明、主要贡献或受诫勉处分的原因及整改要求" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="honorDialogVisible = false">取消</el-button>
        <el-button type="primary" icon="Check" @click="submitSaveHonor">
          {{ isEditingHonor ? '保存修改记录' : '确认登记入库' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 弹窗 7: 新建/发送党务通知弹窗 -->
    <el-dialog 
      v-model="noticeDialogVisible" 
      title="【发布党建通知与合规指令】" 
      width="700px" 
      destroy-on-close
    >
      <el-form label-width="125px">
        <el-form-item label="通知类别" required>
          <el-select v-model="newNoticeForm.noticeType" style="width: 100%">
            <el-option label="合规时限预警 (红线催办)" value="DEADLINE_WARNING" />
            <el-option label="转正到期催办 (预备党员)" value="TRANS_PROBATION" />
            <el-option label="纪检把关通知 (廉洁会签)" value="DISCIPLINE_AUDIT" />
            <el-option label="三会一课通知 (组织生活)" value="MEETING_NOTICE" />
            <el-option label="党建业务通知 (综合性)" value="REGULAR" />
          </el-select>
        </el-form-item>
        <el-form-item label="触达渠道" required>
          <el-select v-model="newNoticeForm.channelCode" style="width: 100%">
            <el-option v-for="ch in noticeChannels.filter(item => item.enabled === 1)" :key="ch.channelCode" :label="ch.channelName" :value="ch.channelCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择系统用户">
          <el-select v-model="newNoticeForm.receiverId" filterable clearable style="width: 100%" placeholder="选择接收人，使用账号中已登记的渠道地址">
            <el-option v-for="user in noticeRecipients" :key="user.id" :label="`${user.realName}（${user.workNo}）`" :value="user.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!newNoticeForm.receiverId && newNoticeForm.channelCode !== 'IN_APP'" label="接收地址" required>
          <el-input v-model="newNoticeForm.receiverTarget" :placeholder="noticeTargetHint(newNoticeForm.channelCode)" />
        </el-form-item>
        <el-alert title="外部通知显示“已受理”表示服务商接收请求，实际送达请结合服务商回执核对。" type="info" :closable="false" style="margin-bottom: 16px" />
        <el-form-item label="通知标题" required>
          <el-input v-model="newNoticeForm.title" placeholder="如 【时限红线】入党谈话即将到期请抓紧推进" />
        </el-form-item>
        <el-form-item label="通知正文内容" required>
          <el-input v-model="newNoticeForm.content" type="textarea" :rows="4" placeholder="填写完整通知事项、纪律要求、参会要求或时间红线说明..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="noticeDialogVisible = false">取消</el-button>
        <el-button type="primary" icon="Promotion" :loading="noticeBusy" @click="submitSendNotice">立即发送</el-button>
      </template>
    </el-dialog>

    <!-- 弹窗 8: 通知渠道参数配置弹窗 -->
    <el-dialog 
      v-model="channelConfigDialogVisible" 
      :title="`【通知渠道配置】${currentEditingChannel?.channelName}`" 
      width="600px" 
      destroy-on-close
    >
      <el-form v-if="currentEditingChannel" label-width="110px">
        <el-form-item label="渠道名称">
          <el-input v-model="currentEditingChannel.channelName" disabled />
        </el-form-item>
        <el-alert :title="NOTICE_CHANNEL_META[currentEditingChannel.channelCode]?.hint" type="info" :closable="false" style="margin-bottom: 16px" />
        <p v-if="currentEditingChannel.channelCode !== 'IN_APP'">密钥由运维人员设置到服务器环境变量中，下方只填写变量名，不填写密钥原文。</p>
        <el-form-item v-for="field in NOTICE_CHANNEL_META[currentEditingChannel.channelCode]?.fields" :key="field.key" :label="field.label" label-width="180px" required>
          <el-select v-if="field.options" v-model="channelForm[field.key]" style="width: 100%">
            <el-option v-for="option in field.options" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
          <el-input v-else v-model="channelForm[field.key]" :placeholder="field.default ? String(field.default) : `请输入${field.label}`" />
        </el-form-item>
        <template v-if="currentEditingChannel.channelCode === 'SMS'">
          <p>按通知类别绑定审核通过的模板；未使用的类别可留空。变量名须与服务商模板完全一致。</p>
          <div v-for="(label, type) in NOTICE_TYPES" :key="type" style="margin-bottom: 18px">
            <el-form-item :label="label" label-width="150px"><el-input v-model="smsTemplates[type].templateCode" placeholder="已审核模板编码，例如 SMS_123456789" /></el-form-item>
            <el-form-item label="模板变量映射" label-width="150px"><el-input v-model="smsTemplates[type].mapping" placeholder='如 {"name":"receiverName","item":"title"}；无变量填 {}' /></el-form-item>
          </div>
          <p>可选变量来源：receiverName（姓名）、title（标题）、content（正文）。正文按原文发送，不自动截断。</p>
        </template>
        <el-link v-if="NOTICE_CHANNEL_META[currentEditingChannel.channelCode]?.doc" :href="NOTICE_CHANNEL_META[currentEditingChannel.channelCode].doc" target="_blank" rel="noopener noreferrer" type="primary">查看服务商官方对接文档</el-link>
        <el-form-item label="说明备注">
          <el-input v-model="currentEditingChannel.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="channelConfigDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="noticeBusy" :disabled="!apiSession" @click="saveChannelConfig">保存配置</el-button>
      </template>
    </el-dialog>

    <!-- 弹窗 9: 查看通知详情弹窗 -->
    <el-dialog 
      v-model="noticeDetailVisible" 
      title="【通知详情与审计痕迹】" 
      width="580px" 
      destroy-on-close
    >
      <div v-if="currentViewingNotice" class="notice-detail-view">
        <h3 class="nd-title">{{ currentViewingNotice.title }}</h3>
        <div class="nd-meta">
          <el-tag :type="currentViewingNotice.typeTag" size="small">{{ currentViewingNotice.noticeTypeName }}</el-tag>
          <span>分发渠道：{{ currentViewingNotice.channelName }}</span>
          <span>时间：{{ currentViewingNotice.sendTime }}</span>
        </div>
        <div class="nd-content">{{ currentViewingNotice.content }}</div>
        <div class="nd-footer">
          <p><strong>接收主体：</strong>{{ currentViewingNotice.receiverName }} ({{ currentViewingNotice.receiverTarget }})</p>
          <p><strong>发送结果：</strong>{{ noticeStatus(currentViewingNotice) }}</p>
          <p v-if="currentViewingNotice.providerMessageId"><strong>服务商消息 / 任务编号：</strong>{{ currentViewingNotice.providerMessageId }}</p>
          <p v-if="currentViewingNotice.errorMsg"><strong>核对提示：</strong>{{ currentViewingNotice.errorMsg }}</p>
        </div>
      </div>
      <template #footer>
        <el-button type="warning" plain icon="Bell" @click="goToNoticeCenter">进入通知中心管理全部</el-button>
        <el-button type="primary" @click="noticeDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 弹窗 10: 新建/编辑党务用户 -->
    <el-dialog 
      v-model="userDialogVisible" 
      :title="isEditingUser ? '【编辑党务账号信息】' : '【新建党务系统账号】'" 
      width="680px" 
      destroy-on-close
    >
      <el-form label-width="125px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="登录账号" required>
              <el-input v-model="userForm.username" :disabled="isEditingUser" placeholder="英文小写账号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="真实姓名" required>
              <el-input v-model="userForm.realName" placeholder="党员或干部姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="企业工号" required>
              <el-input v-model="userForm.workNo" placeholder="如 HH-JT-009" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" required>
              <el-input v-model="userForm.phone" placeholder="手机号 (接收短信通知)" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="所属党组织" required>
          <el-select v-model="userForm.orgName" style="width: 100%">
            <el-option label="红河红数信息技术服务有限公司支部委员会" value="中共红河红数信息技术服务有限公司支部委员会" />
            <el-option label="云南幂次科技有限公司支部委员会" value="中共云南幂次科技有限公司支部委员会" />
            <el-option label="红河链达科技有限公司支部委员会" value="中共红河链达科技有限公司支部委员会" />
            <el-option label="红河数据产业集团有限公司总支部委员会" value="中共红河数据产业集团有限公司总支部委员会" />
          </el-select>
        </el-form-item>
        <el-form-item label="赋予角色" required>
          <el-select v-model="userForm.roleCode" style="width: 100%" @change="handleUserFormRoleChange">
            <el-option v-for="r in sysRoles" :key="r.id" :label="r.roleName" :value="r.roleCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="电子邮箱">
          <el-input v-model="userForm.email" placeholder="企业内网邮箱" />
        </el-form-item>
        <el-form-item label="企业微信账号">
          <el-input v-model="userForm.wecomUserId" placeholder="企业微信通讯录中的成员 UserID" />
        </el-form-item>
        <el-form-item label="钉钉成员账号">
          <el-input v-model="userForm.dingtalkUserId" placeholder="钉钉通讯录中的成员 UserID，不是邮箱" />
        </el-form-item>
        <el-form-item v-if="apiSession" :label="isEditingUser ? '重置密码' : '登录密码'">
          <el-input v-model="userForm.password" type="password" show-password autocomplete="new-password" :placeholder="isEditingUser ? '留空则保留原密码' : '至少8位，含字母、数字和特殊字符'" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" icon="Check" @click="saveSysUser">
          {{ isEditingUser ? '保存修改' : '确认创建账号' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 弹窗 11: 角色授权矩阵弹窗 -->
    <el-dialog 
      v-model="roleAuthDialogVisible" 
      :title="`【为党员分配角色与权限】${currentAuthorizingUser?.realName}`" 
      width="540px" 
      destroy-on-close
    >
      <div v-if="currentAuthorizingUser">
        <p style="margin-bottom: 12px; color: #606266;">
          正在为 <strong>{{ currentAuthorizingUser.realName }}</strong>（工号：{{ currentAuthorizingUser.workNo }}）重新分配角色与权限：
        </p>
        <el-radio-group v-model="selectedRoleCodeForAssign" style="display: flex; flex-direction: column; gap: 10px;">
          <el-radio v-for="r in sysRoles" :key="r.id" :value="r.roleCode">
            <strong>{{ r.roleName }}</strong> - <span style="color: #909399; font-size: 12px;">{{ r.description.slice(0, 32) }}...</span>
          </el-radio>
        </el-radio-group>
      </div>
      <template #footer>
        <el-button @click="roleAuthDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAssignRole">确认授权生效</el-button>
      </template>
    </el-dialog>

    <!-- 弹窗 12: 入党申请人建档弹窗 -->
    <el-dialog 
      v-model="addApplicantDialogVisible" 
      title="【入党申请人建档】录入第 1 步《递交入党申请书》" 
      width="820px" 
      destroy-on-close
    >
      <el-alert
        title="规程提醒：申请人须年满18周岁、自愿提出书面亲笔申请；支部收到申请后须在 1 个月内指派专人完成初次政治谈话并归档。"
        type="warning"
        :closable="false"
        style="margin-bottom: 12px"
      />
      <div class="roster-filing-tip-box" style="margin-bottom: 16px; padding: 10px 14px; background: #eff6ff; border: 1px dashed #3b82f6; border-radius: 6px; font-size: 12px; color: #1d4ed8; display: flex; align-items: center; gap: 8px;">
        <el-icon><InfoFilled /></el-icon>
        <span><strong>全流程联动说明：</strong>立卷建档后系统将自动为申请人开通工号系统账号（初始密码123456），并纳入【发展党员全景工作台】开启 25 步规范化考察；待全部 25 步考察合规并经党总支审批转正后，将自动正式同步入库至【党员花名册】。</span>
      </div>
      <el-form :model="newApplicantForm" label-width="140px" class="member-add-form">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="申请人姓名" required>
              <el-input v-model="newApplicantForm.name" placeholder="员工真实姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="企业工号" required>
              <el-input v-model="newApplicantForm.workNo" placeholder="如 HH-HS-088" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="公民身份证号" required>
              <el-input v-model="newApplicantForm.idCard" placeholder="18位公民身份证号" maxlength="18" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" required>
              <el-input v-model="newApplicantForm.phone" placeholder="手机号码（用于通知与账号绑定）" maxlength="11" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="性别 / 年龄" required>
              <div style="display: flex; gap: 10px; width: 100%;">
                <el-select v-model="newApplicantForm.gender" style="width: 90px">
                  <el-option label="男" value="男" />
                  <el-option label="女" value="女" />
                </el-select>
                <el-input-number v-model="newApplicantForm.age" :min="18" :max="70" style="flex: 1" />
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="民族">
              <el-select v-model="newApplicantForm.nation" placeholder="请选择民族" style="width: 100%">
                <el-option label="汉族" value="汉族" />
                <el-option label="彝族" value="彝族" />
                <el-option label="哈尼族" value="哈尼族" />
                <el-option label="白族" value="白族" />
                <el-option label="傣族" value="傣族" />
                <el-option label="苗族" value="苗族" />
                <el-option label="壮族" value="壮族" />
                <el-option label="回族" value="回族" />
                <el-option label="其他少数民族" value="其他少数民族" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="籍贯">
              <el-input v-model="newApplicantForm.nativePlace" placeholder="如 云南蒙自 / 云南昆明" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最高学历" required>
              <el-select v-model="newApplicantForm.education" placeholder="最高学历" style="width: 100%">
                <el-option label="大专" value="大专" />
                <el-option label="大学本科" value="大学本科" />
                <el-option label="硕士研究生" value="硕士研究生" />
                <el-option label="博士研究生" value="博士研究生" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="所属党支部" required>
              <!-- 若为支部管理员，强锁本支部且禁用修改，杜绝跨支部建档 -->
              <el-select 
                v-model="newApplicantForm.branchName" 
                @change="resetEmployment(newApplicantForm)"
                :disabled="isBranchAdmin"
                style="width: 100%"
              >
                <el-option label="红数信息支部" value="中共红河红数信息技术服务有限公司支部委员会" />
                <el-option label="幂次科技支部" value="中共云南幂次科技有限公司支部委员会" />
                <el-option label="链达科技支部" value="中共红河链达科技有限公司支部委员会" />
                <el-option label="集团党总支" value="中共红河数据产业集团有限公司总支部委员会" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="企业行政部门" required>
              <el-select 
                v-model="newApplicantForm.deptName" 
                filterable 
                placeholder="请选择行政部门（支持跨公司选择）" 
                style="width: 100%" 
                @change="suggestJobTitle(newApplicantForm)"
              >
                <el-option-group 
                  v-for="group in departmentGroups" 
                  :key="group.companyName" 
                  :label="group.companyName"
                >
                  <el-option 
                    v-for="dept in group.options" 
                    :key="dept.value" 
                    :label="dept.value" 
                    :value="dept.value" 
                  />
                </el-option-group>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 跨单位派驻/挂靠智能提示条 -->
        <div v-if="isCrossCompany(newApplicantForm.branchName, newApplicantForm.deptName)" class="cross-unit-alert">
          <el-icon><InfoFilled /></el-icon>
          <span>
            <strong>【跨单位派驻/挂靠档案】</strong>
            该同志人事关系在<strong>【{{ getCompanyNameFromDept(newApplicantForm.deptName) }}】</strong>，
            党组织关系编入<strong>【{{ newApplicantForm.branchName }}】</strong>。系统将自动建立跨单位派驻/挂靠标记并同步花名册。
          </span>
        </div>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="岗位职务" required>
              <el-input v-model="newApplicantForm.jobTitle" placeholder="请输入岗位职务，可修改推荐岗位" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="参加工作时间">
              <el-date-picker 
                v-model="newApplicantForm.workDate" 
                type="date" 
                value-format="YYYY-MM-DD" 
                placeholder="入职或参加工作日期"
                style="width: 100%" 
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="递交申请日期" required>
              <el-date-picker 
                v-model="newApplicantForm.applyDate" 
                type="date" 
                value-format="YYYY-MM-DD" 
                style="width: 100%" 
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="国企骨干属性">
          <div style="display: flex; flex-wrap: wrap; gap: 18px; align-items: center; width: 100%;">
            <el-checkbox v-model="newApplicantForm.isFrontline">生产/业务一线骨干</el-checkbox>
            <el-checkbox v-model="newApplicantForm.isTechnicalTalent">数字研发核心技术骨干</el-checkbox>
            <el-checkbox v-model="newApplicantForm.isDualCultivate">列入“双培养”工程</el-checkbox>
          </div>
        </el-form-item>

        <el-form-item label="书面申请书原件">
          <el-upload
            action="#"
            :auto-upload="false"
            :limit="1"
            accept=".pdf,.doc,.docx,.jpg,.png"
          >
            <el-button size="small" type="primary" plain icon="Upload">上传亲笔签名扫描件 (.pdf / 图片)</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addApplicantDialogVisible = false">取消</el-button>
        <el-button type="primary" icon="Check" @click="submitCreateApplicant">确认立卷建档</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import BigScreenView from './components/BigScreenView.vue'
import LoginView from './components/LoginView.vue'
import PartyEmblem from './components/PartyEmblem.vue'
import { getCompanyByBranch, getDepartmentOptions, getAllDepartmentGroupOptions, isCrossCompany, getCompanyNameFromDept } from './data/companyDepartments.js'
import { apiRequest, apiSession, clearApiSession, demoEnabled } from './api.js'
import { NOTICE_CHANNEL_META, NOTICE_TYPES, noticeStatus } from './data/noticeChannels.js'
import { 
  SOE_ORGS, 
  STAGES_AND_STEPS, 
  MOCK_MEMBERS, 
  ANNUAL_QUOTA, 
  ALL_MEMBERS_ROSTER, 
  TEMPLATES_CATALOG_25,
  MOCK_MEETING_RECORDS,
  AVAILABLE_TOPIC_TAGS,
  MOCK_HONOR_PUNISHMENT_LIST,
  MOCK_SYS_ROLES,
  MOCK_SYS_USERS,
  MOCK_RELATION_TRANSFERS,
  MOCK_POSITION_ADJUSTMENTS,
} from './data/mockData.js'

// 基础模式、大屏状态与登录态
const isBigScreenMode = ref(false)
const isLoggedIn = ref(demoEnabled)
const currentUser = ref(MOCK_SYS_USERS[1]) // 默认杨海 (党总支管理员)
const currentRole = ref('general_branch_admin')
const activeTab = ref('workbench')

// ==========================================
// 四级精简权限体系核心规则与鉴权函数
// 1. 超级管理员 (sys_admin): 唯一独享用户管理、通知渠道配置，拥有全平台所有权限
// 2. 党总支管理员 (general_branch_admin): 拥有总支及直管三支部全部业务权限，独享第20/25步审批批复、跨支部指标调控、一人一档归档、模板导入
// 3. 支部管理员 (branch_admin_hs / branch_admin_mc / branch_admin_ld): 仅限本支部业务增删改查（表单强锁本支部，不可审批第20/25步）
// 4. 普通党员 (party_member): 仅限调阅本人档案与个人通知
// ==========================================
const isBranchAdmin = computed(() => {
  return currentRole.value.startsWith('branch_admin')
})

const currentBranchNameLocked = computed(() => {
  if (currentRole.value === 'branch_admin_hs') return '中共红河红数信息技术服务有限公司支部委员会'
  if (currentRole.value === 'branch_admin_mc') return '中共云南幂次科技有限公司支部委员会'
  if (currentRole.value === 'branch_admin_ld') return '中共红河链达科技有限公司支部委员会'
  return currentUser.value?.orgName || '中共红河数据产业集团有限公司总支部委员会'
})

const currentRoleDisplayName = computed(() => {
  const map = {
    sys_admin: '超级管理员 (系统全权限)',
    general_branch_admin: '党总支管理员 (总支及三支部业务全权限)',
    branch_admin_hs: '支部管理员 (红数信息支部)',
    branch_admin_mc: '支部管理员 (云南幂次科技支部)',
    branch_admin_ld: '支部管理员 (红河链达科技支部)',
    party_member: '普通党员 / 发展成员本人'
  }
  return map[currentRole.value] || '党务在册人员'
})

const currentRoleTagType = computed(() => {
  const map = {
    sys_admin: 'info',
    general_branch_admin: 'danger',
    branch_admin_hs: 'warning',
    branch_admin_mc: 'warning',
    branch_admin_ld: 'warning',
    party_member: ''
  }
  return map[currentRole.value] || 'info'
})

/**
 * 判断当前登录人是否拥有某项细粒度权限标识
 */
function hasPermission(perm) {
  if (!perm) return true

  // 1. 超级管理员拥有全平台所有权限 (唯一独享用户管理、通知渠道配置)
  if (currentRole.value === 'sys_admin') {
    return true
  }

  // 2. 党总支管理员：拥有总支及三支部全部党务业务权限 (不含用户管理与通知渠道底层配置)
  if (currentRole.value === 'general_branch_admin') {
    return [
      'workbench:view', 'workbench:create_applicant', 'workbench:advance', 'workbench:audit', 
      'workbench:transfer', 'workbench:export', 'workbench:block_override',
      'roster:view', 'roster:create', 'roster:edit', 'roster:import', 'roster:export',
      'meeting:view', 'meeting:create', 'meeting:edit', 'meeting:delete', 'meeting:tags_manage', 'meeting:export',
      'honor:view', 'honor:create', 'honor:edit', 'honor:delete', 'honor:export',
      'template:view', 'template:upload', 'template:reset', 'cockpit:view', 'notice:view', 'notice:send'
    ].includes(perm)
  }

  // 3. 支部管理员：拥有本支部业务增删改查权限 (仅限本支部数据与表单)
  if (isBranchAdmin.value) {
    return [
      'workbench:view', 'workbench:create_applicant', 'workbench:advance', 'workbench:export',
      'roster:view', 'roster:create', 'roster:edit', 'roster:import', 'roster:export',
      'meeting:view', 'meeting:create', 'meeting:edit', 'meeting:delete', 'meeting:export',
      'honor:view', 'honor:create', 'honor:edit', 'honor:delete', 'honor:export',
      'template:view', 'cockpit:view', 'notice:view'
    ].includes(perm)
  }

  // 4. 普通党员：仅限自我查阅
  if (currentRole.value === 'party_member') {
    return ['workbench:view', 'notice:view', 'cockpit:view'].includes(perm)
  }

  return false
}

/**
 * 校验当前登录人是否拥有某个角色
 */
function hasRole(roles) {
  if (!roles || !roles.length) return true
  if (Array.isArray(roles)) {
    return roles.includes(currentRole.value)
  }
  return currentRole.value === roles
}

const currentUserOrgShort = computed(() => {
  if (!currentUser.value?.orgName) return '集团党总支'
  if (currentUser.value.orgName.includes('红数')) return '红数信息支部'
  if (currentUser.value.orgName.includes('幂次')) return '幂次科技支部'
  if (currentUser.value.orgName.includes('链达')) return '链达科技支部'
  return '集团党总支'
})

/**
 * 顶部一体化主导航菜单配置 (严格对应9大一级业务模块与细粒度权限控制)
 */
const navMenuItems = computed(() => [
  {
    name: 'workbench',
    label: '发展党员全景工作台',
    icon: 'Operation',
    visible: hasPermission('workbench:view')
  },
  {
    name: 'roster',
    label: '党员花名册',
    icon: 'User',
    visible: hasPermission('roster:view')
  },
  {
    name: 'transfer_filing',
    label: '党员转接及调整备案',
    icon: 'Switch',
    visible: hasPermission('roster:view') || hasPermission('workbench:transfer')
  },
  {
    name: 'meetings',
    label: '“三会一课”/组织生活',
    icon: 'Calendar',
    visible: hasPermission('meeting:view')
  },
  {
    name: 'honors',
    label: '组织/个人奖惩或荣誉',
    icon: 'Trophy',
    visible: hasPermission('honor:view')
  },
  {
    name: 'templates',
    label: '文书知识库',
    icon: 'DocumentCopy',
    visible: hasPermission('template:view')
  },
  {
    name: 'cockpit',
    label: '数据驾驶舱',
    icon: 'DataAnalysis',
    visible: hasPermission('cockpit:view')
  },
  {
    name: 'notices',
    label: '通知中心',
    icon: 'BellFilled',
    visible: hasPermission('notice:view')
  },
  {
    name: 'users',
    label: '党务用户管理',
    icon: 'Avatar',
    visible: hasRole('sys_admin')
  }
])

function handleLoginSuccess(payload) {
  currentUser.value = payload.user
  currentRole.value = payload.role
  isLoggedIn.value = true

  // 统一固定登录后默认进入【发展党员全景工作台】
  activeTab.value = 'workbench'
}

function handleLogout() {
  ElMessageBox.confirm(
    '确定要安全退出当前党务登录状态，返回身份认证登录页吗？',
    '退出登录确认',
    { confirmButtonText: '确定退出', cancelButtonText: '取消', type: 'info' }
  ).then(() => {
    clearApiSession()
    isLoggedIn.value = false
    ElMessage.success('已安全退出系统')
  }).catch(() => {})
}

// ==========================================
// 党务合规雷达从右往左滚动与交互直达
// ==========================================
const BASE_RADAR_ALERTS = [
  {
    id: 1,
    title: '【合规阻断】入党积极分子考察期不满 365 天强制锁定',
    badgeText: '硬阻断',
    tagType: 'danger',
    alertClass: 'alert-danger',
    summary: '【张强·红数科技】积极分子考察仅 290 天（未满 365 天），系统强制锁定禁止提前流转至第 9 步发展对象！',
    noticeTypeName: '合规时限预警',
    channelName: '企业微信',
    receiverName: '李卫民 (红数信息支部书记)',
    receiverTarget: 'liweimin@hongshu-info.com',
    sendTime: '2026-10-09 09:30:00',
    content: '【张强】同志积极分子备案时间为 2024-06-15，截至今日考察仅 290 天，未满法定 1 年硬性考察周期，系统合规防错引擎已强制阻断进入第 9 步！'
  },
  {
    id: 2,
    title: '【时限红线】入党申请谈话 30 天红线临期预警',
    badgeText: '临期预警',
    tagType: 'warning',
    alertClass: 'alert-warning',
    summary: '【陈思佳·集团总部】申请书递交已第 22 天，距离“1个月内必须完成支部谈话”红线仅剩 8 天！',
    noticeTypeName: '合规时限预警',
    channelName: '政务短信',
    receiverName: '杨海 (总支组织委员)',
    receiverTarget: '13987301005',
    sendTime: '2026-10-09 08:45:12',
    content: '【陈思佳】同志于 2025-03-15 递交入党申请书，已满 22 天，距离中组部细则“1个月内必须指派专人谈话”红线仅剩 8 天，请支部抓紧开展谈话并归档谈话记录表。'
  },
  {
    id: 3,
    title: '【转正催办】预备党员预备期届满提醒及转正申请催办',
    badgeText: '转正提醒',
    tagType: 'warning',
    alertClass: 'alert-warning',
    summary: '【李晓辉·链达科技】预备期将在 14 天后满 1 年，已自动下发《转正申请书》催办指令。',
    noticeTypeName: '转正到期催办',
    channelName: '企业微信',
    receiverName: '李晓辉 (预备党员)',
    receiverTarget: 'HH-LD-008',
    sendTime: '2026-10-08 14:20:00',
    content: '预备党员【李晓辉】同志预备期（2024-03-25 ~ 2025-03-25）即将满期，已自动下达转正催办通知，请本人于满期前1-2周主动向链达科技党支部递交书面《转正申请书》。'
  },
  {
    id: 4,
    title: '【纪检会签】发展对象廉洁从业审查意见书待出具',
    badgeText: '纪检联动',
    tagType: 'primary',
    alertClass: 'alert-info',
    summary: '【林雨涵·幂次科技】政审函调完成，当前流转至集团纪委出具《廉洁从业意见书》（一票否决权）。',
    noticeTypeName: '纪检把关通知',
    channelName: '钉钉工作通知',
    receiverName: '周国平 (总支纪检委员)',
    receiverTarget: 'zhouguoping@honghe-data.com',
    sendTime: '2026-10-07 10:15:30',
    content: '发展对象【林雨涵】同志已完成直系亲属政审函调，当前流转至集团纪委出具《廉洁从业意见书》（一票否决权），请风险管控部周国平部长在线复核会签。'
  }
]

// 复制两组实现无缝循环左移跑马灯
const radarAlertsDoubled = computed(() => {
  return [...BASE_RADAR_ALERTS, ...BASE_RADAR_ALERTS]
})

function handleRadarAlertClick(alertItem) {
  currentViewingNotice.value = {
    id: alertItem.id,
    title: alertItem.title,
    noticeTypeName: alertItem.noticeTypeName,
    typeTag: alertItem.tagType,
    channelName: alertItem.channelName,
    sendTime: alertItem.sendTime,
    content: alertItem.content,
    receiverName: alertItem.receiverName,
    receiverTarget: alertItem.receiverTarget,
    isRead: 1
  }
  noticeDetailVisible.value = true
}

function goToNoticeCenter() {
  noticeDetailVisible.value = false
  activeTab.value = 'notices'
  ElMessage.info('已为您切换至【党建通知中心与多渠道】管理页')
}

// 全集团所有法人单位与行政部门分组（支持人在A公司、党组织在B支部的跨单位自由选择）
const departmentGroups = getAllDepartmentGroupOptions()

// ==========================================
// 1. 发展党员工作台与新建申请人业务逻辑
// ==========================================
function resetEmployment(form) {
  // 如果尚未选择部门，或者需要默认推荐，带出支部所在公司默认部门
  if (!form.deptName) {
    form.deptName = getDepartmentOptions(form.branchName)[0]?.value || ''
    suggestJobTitle(form)
  }
}

function suggestJobTitle(form) {
  if (!form.deptName) return
  // 先从该支部对应公司寻找建议岗位
  let found = getDepartmentOptions(form.branchName).find(dept => dept.value === form.deptName)
  // 若为跨公司选择的部门，从全集团部门分组中匹配对应建议岗位
  if (!found) {
    for (const group of departmentGroups) {
      found = group.options.find(dept => dept.value === form.deptName)
      if (found) break
    }
  }
  if (found?.jobTitle) {
    form.jobTitle = found.jobTitle
  }
}

function validateEmployment(form) {
  if (!form.deptName || !form.deptName.trim()) {
    ElMessage.warning('请选择人事所属单位及行政部门！')
    return false
  }
  if (!form.jobTitle?.trim()) {
    ElMessage.warning('请输入岗位职务！')
    return false
  }
  return true
}

const membersList = ref([...MOCK_MEMBERS])
const addApplicantDialogVisible = ref(false)
const newApplicantForm = ref({
  name: '',
  workNo: '',
  idCard: '',
  phone: '',
  gender: '男',
  age: 26,
  nation: '汉族',
  nativePlace: '',
  education: '大学本科',
  branchName: '中共红河红数信息技术服务有限公司支部委员会',
  deptName: '',
  jobTitle: '',
  workDate: '2023-07-01',
  applyDate: new Date().toISOString().slice(0, 10),
  isFrontline: true,
  isTechnicalTalent: true,
  isDualCultivate: false
})

const selectedFilterStage = ref(null)
const filterBranch = ref('')
const filterSpecialType = ref('')
const searchKeyword = ref('')

const filteredMembers = computed(() => {
  return membersList.value.filter(m => {
    // 四级数据范围限制：支部管理员仅能查阅本支部发展成员
    if (currentRole.value === 'branch_admin_hs' && !m.branchName.includes('红数')) return false
    if (currentRole.value === 'branch_admin_mc' && !m.branchName.includes('幂次')) return false
    if (currentRole.value === 'branch_admin_ld' && !m.branchName.includes('链达')) return false
    // 普通党员仅能查看本人的成长档案
    if (currentRole.value === 'party_member' && m.workNo !== (currentUser.value?.workNo || 'HH-HS-012')) return false

    if (selectedFilterStage.value && m.currentStageId !== selectedFilterStage.value) return false
    if (filterBranch.value && m.branchName !== filterBranch.value) return false
    if (filterSpecialType.value === 'frontline' && !m.isFrontline) return false
    if (filterSpecialType.value === 'technical' && !m.isTechnicalTalent) return false
    if (filterSpecialType.value === 'dual' && !m.isDualCultivate) return false
    if (searchKeyword.value) {
      const kw = searchKeyword.value.toLowerCase()
      const matchName = m.name.toLowerCase().includes(kw)
      const matchNo = m.workNo.toLowerCase().includes(kw)
      const matchDept = m.deptName.toLowerCase().includes(kw)
      const matchJob = m.jobTitle.toLowerCase().includes(kw)
      if (!matchName && !matchNo && !matchDept && !matchJob) return false
    }
    return true
  })
})

function getStageMemberCount(stageId) {
  return membersList.value.filter(m => m.currentStageId === stageId).length
}

function openAddDialog() {
  const defaultBranch = isBranchAdmin.value ? currentBranchNameLocked.value : '中共红河红数信息技术服务有限公司支部委员会'
  newApplicantForm.value = {
    name: '',
    workNo: 'HH-' + (Math.floor(Math.random() * 890 + 100)),
    idCard: '',
    phone: '',
    gender: '男',
    age: 26,
    nation: '汉族',
    nativePlace: '云南红河',
    education: '大学本科',
    branchName: defaultBranch,
    deptName: '',
    jobTitle: '',
    workDate: '2023-07-01',
    applyDate: new Date().toISOString().slice(0, 10),
    isFrontline: true,
    isTechnicalTalent: true,
    isDualCultivate: false
  }
  resetEmployment(newApplicantForm.value)
  addApplicantDialogVisible.value = true
}

function submitCreateApplicant() {
  if (!newApplicantForm.value.name.trim()) {
    ElMessage.warning('请输入入党申请人真实姓名！')
    return
  }
  if (!newApplicantForm.value.workNo.trim()) {
    ElMessage.warning('请输入员工工号！')
    return
  }
  if (!newApplicantForm.value.idCard.trim() || newApplicantForm.value.idCard.trim().length !== 18) {
    ElMessage.warning('请输入完整的18位公民身份证号码！')
    return
  }
  if (!newApplicantForm.value.phone.trim()) {
    ElMessage.warning('请输入联系电话（手机号码）！')
    return
  }
  if (!validateEmployment(newApplicantForm.value)) return
  if (!newApplicantForm.value.applyDate) {
    ElMessage.warning('请选择递交入党申请书日期！')
    return
  }

  // 支部管理员越权校验：禁止为其他支部建档
  if (isBranchAdmin.value && newApplicantForm.value.branchName !== currentBranchNameLocked.value) {
    ElMessageBox.alert('您无权跨支部为其他党支部建立入党申请人档案！', '越权拦截', { type: 'error' })
    return
  }

  const newId = Date.now()

  const applicantRecord = {
    id: newId,
    name: newApplicantForm.value.name.trim(),
    workNo: newApplicantForm.value.workNo.trim(),
    idCard: newApplicantForm.value.idCard.trim(),
    phone: newApplicantForm.value.phone.trim(),
    gender: newApplicantForm.value.gender,
    age: newApplicantForm.value.age,
    nation: newApplicantForm.value.nation || '汉族',
    nativePlace: newApplicantForm.value.nativePlace || '',
    deptName: newApplicantForm.value.deptName.trim(),
    jobTitle: newApplicantForm.value.jobTitle.trim(),
    workDate: newApplicantForm.value.workDate || '',
    education: newApplicantForm.value.education,
    branchId: getCompanyByBranch(newApplicantForm.value.branchName).orgId,
    branchName: newApplicantForm.value.branchName,
    originBranch: '企业新录入入党申请人',
    transferInDate: newApplicantForm.value.applyDate,
    transferOutDate: null,
    transferOutBranch: '',
    isFrontline: newApplicantForm.value.isFrontline,
    isTechnicalTalent: newApplicantForm.value.isTechnicalTalent,
    isDualCultivate: newApplicantForm.value.isDualCultivate,
    cultivators: [],
    currentStageId: 1, // 阶段1: 申请入党
    currentStepId: 1,  // 步骤1: 递交入党申请书
    stepStatus: 'process',
    daysInCurrentStep: 1,
    applyDate: newApplicantForm.value.applyDate,
    firstTalkDate: null,
    activistDate: null,
    targetDate: null,
    probationaryDate: null,
    officialDate: null,
    complianceAlert: {
      type: 'warning',
      message: '【新入党申请人建档】请支部在申请之日起 1 个月内指派专人开展谈话并归档谈话记录表。'
    },
    materials: [
      { name: '入党申请书(亲笔书面原件)', code: 'M01', status: 'approved', time: newApplicantForm.value.applyDate }
    ]
  }

  // 1. 注入发展党员工作台列表 (归入阶段1全流程管理)
  membersList.value.unshift(applicantRecord)

  // 2. 账号系统联动：自动为新申请人开通系统登录账号 (工号为账号，弱密123456，标记强制改密，绑定录入手机号)
  const existingUserIdx = sysUsers.value.findIndex(u => u.workNo === applicantRecord.workNo)
  if (existingUserIdx === -1) {
    sysUsers.value.unshift({
      id: Date.now() + 10,
      username: applicantRecord.workNo.toLowerCase(),
      password: '123456',
      mustChangePwd: true, // 首次登录强制改密
      realName: applicantRecord.name,
      workNo: applicantRecord.workNo,
      phone: applicantRecord.phone,
      email: `${applicantRecord.workNo.toLowerCase()}@honghe-data.com`,
      orgId: applicantRecord.branchId || 2,
      orgName: applicantRecord.branchName,
      roleCode: 'PARTY_MEMBER',
      roleName: '普通在册党员 / 发展成员本人',
      status: 1,
      lastLoginTime: '未登录',
      createdAt: new Date().toISOString().slice(0, 10)
    })
  }

  // 3. 花名册系统联动：据实同步录入【党员花名册】，政治面貌记为【入党申请人】（partyStatus: 5）
  syncMemberToRoster(applicantRecord, 5, {
    partyPost: '入党申请人',
    idCard: applicantRecord.idCard,
    phone: applicantRecord.phone,
    nation: applicantRecord.nation,
    nativePlace: applicantRecord.nativePlace,
    workDate: applicantRecord.workDate
  })

  addApplicantDialogVisible.value = false
  ElMessage.success(`【${applicantRecord.name}】同志建档成功！已纳入【发展党员全景工作台】开启 25 步全流程培养考察，已据实同步入库【党员花名册】（政治面貌：入党申请人），并为其自动开通系统账号（工号：${applicantRecord.workNo}，初始密码 123456）。后续随着培养阶段推进，花名册状态将实时联动升级！`)
}

function filterByStage(stageId) {
  selectedFilterStage.value = selectedFilterStage.value === stageId ? null : stageId
}

function resetFilters() {
  selectedFilterStage.value = null
  filterBranch.value = ''
  filterSpecialType.value = ''
  searchKeyword.value = ''
}

// ==========================================
// 2. 所有党员花名册逻辑与录入导入
// ==========================================
const rosterList = ref([...ALL_MEMBERS_ROSTER])

// 党员花名册与发展流程据实同步函数 (覆盖发展全生命周期：申请人 -> 积极分子 -> 发展对象 -> 预备党员 -> 正式党员)
function syncMemberToRoster(mem, status, extra = {}) {
  const existRoster = rosterList.value.find(r => r.workNo === mem.workNo)
  const statusPostMap = {
    5: '入党申请人',
    4: '积极分子',
    3: '发展对象',
    2: '预备党员',
    1: '普通党员'
  }
  const postName = extra.partyPost || (existRoster && existRoster.partyPost && !['入党申请人', '积极分子', '发展对象', '预备党员', '普通党员'].includes(existRoster.partyPost) ? existRoster.partyPost : (statusPostMap[status] || '入党申请人'))

  if (existRoster) {
    existRoster.partyStatus = status
    existRoster.partyPost = postName
    if (extra.joinPartyDate) existRoster.joinPartyDate = extra.joinPartyDate
    if (extra.officialPartyDate) existRoster.officialPartyDate = extra.officialPartyDate
    if (extra.idCard && !existRoster.idCard) existRoster.idCard = extra.idCard
    if (extra.phone && !existRoster.phone) existRoster.phone = extra.phone
    if (extra.nation && !existRoster.nation) existRoster.nation = extra.nation
    if (extra.nativePlace && !existRoster.nativePlace) existRoster.nativePlace = extra.nativePlace
    if (extra.workDate && !existRoster.workDate) existRoster.workDate = extra.workDate
    if (extra.jobTitle) existRoster.jobTitle = extra.jobTitle
    if (extra.deptName) existRoster.deptName = extra.deptName
    if (extra.branchName) existRoster.branchName = extra.branchName
  } else {
    rosterList.value.unshift({
      id: Date.now() + Math.floor(Math.random() * 100),
      name: mem.name,
      workNo: mem.workNo,
      idCard: mem.idCard || extra.idCard || ('532501199' + Math.floor(Math.random() * 89000000 + 10000000)),
      phone: mem.phone || extra.phone || '',
      nation: mem.nation || extra.nation || '汉族',
      nativePlace: mem.nativePlace || extra.nativePlace || '',
      workDate: mem.workDate || extra.workDate || '',
      education: mem.education || '大学本科',
      gender: mem.gender || '男',
      age: mem.age || 28,
      branchName: mem.branchName,
      deptName: mem.deptName,
      jobTitle: mem.jobTitle,
      partyStatus: status,
      partyPost: postName,
      partyStandingYears: 0,
      joinPartyDate: extra.joinPartyDate || null,
      officialPartyDate: extra.officialPartyDate || null,
      duesStatus: 1,
      nationalCode: '53250100' + Math.floor(Math.random() * 89999999 + 10000000),
      studyHours: status === 1 ? 40 : (status === 2 ? 35 : (status === 3 ? 30 : 20)),
      studyTarget: 40,
      isFrontline: mem.isFrontline || false,
      isTechnicalTalent: mem.isTechnicalTalent || false,
      isDualCultivate: mem.isDualCultivate || false
    })
  }
}

const rosterSearchKeyword = ref('')
const rosterBranchFilter = ref('')
const rosterStatusFilter = ref('')

const filteredRosterList = computed(() => {
  return rosterList.value.filter(m => {
    // 四级数据范围限制：支部管理员仅查看本支部在册党员
    if (currentRole.value === 'branch_admin_hs' && !m.branchName.includes('红数')) return false
    if (currentRole.value === 'branch_admin_mc' && !m.branchName.includes('幂次')) return false
    if (currentRole.value === 'branch_admin_ld' && !m.branchName.includes('链达')) return false
    if (currentRole.value === 'party_member' && m.workNo !== (currentUser.value?.workNo || 'HH-HS-012')) return false

    if (rosterBranchFilter.value && m.branchName !== rosterBranchFilter.value) return false
    if (rosterStatusFilter.value && m.partyStatus !== rosterStatusFilter.value) return false
    if (rosterSearchKeyword.value) {
      const kw = rosterSearchKeyword.value.toLowerCase()
      const matchName = m.name.toLowerCase().includes(kw)
      const matchNo = m.workNo.toLowerCase().includes(kw)
      const matchDept = m.deptName.toLowerCase().includes(kw)
      const matchPost = m.partyPost.toLowerCase().includes(kw)
      if (!matchName && !matchNo && !matchDept && !matchPost) return false
    }
    return true
  })
})

function getRosterCountByStatus(status) {
  return rosterList.value.filter(m => m.partyStatus === status).length
}

function getRosterPartyStatusTag(status) {
  const map = { 1: 'danger', 2: 'warning', 3: 'primary', 4: 'info', 5: '' }
  return map[status] || 'info'
}

function getRosterPartyStatusText(status) {
  const map = { 1: '正式党员', 2: '预备党员', 3: '发展对象', 4: '积极分子', 5: '入党申请人' }
  return map[status] || '未知'
}

function resetRosterFilters() {
  rosterSearchKeyword.value = ''
  rosterBranchFilter.value = ''
  rosterStatusFilter.value = ''
}

function exportRosterExcel() {
  ElMessage.success(`已成功导出【红河数据产业集团有限公司党总支全体在册党员花名册】(共 ${rosterList.value.length} 人，.xlsx 格式)`)
}

// 自动根据入党时间计算党龄 (年)
function calculatePartyStandingYears(joinDate) {
  if (!joinDate) return 0
  const join = new Date(joinDate)
  const now = new Date()
  let years = now.getFullYear() - join.getFullYear()
  if (now.getMonth() < join.getMonth() || (now.getMonth() === join.getMonth() && now.getDate() < join.getDate())) {
    years--
  }
  return Math.max(0, years)
}

// 单条新增与修改党员档案逻辑
const addMemberDialogVisible = ref(false)
const isEditingMember = ref(false)
const editingMemberId = ref(null)
const newMemberForm = ref({
  name: '',
  workNo: '',
  idCard: '',
  gender: '男',
  age: 30,
  branchName: '中共红河红数信息技术服务有限公司支部委员会',
  deptName: '',
  jobTitle: '',
  partyStatus: 1,
  partyPost: '普通党员',
  partyStandingYears: 0,
  joinPartyDate: '',
  officialPartyDate: '',
  nationalCode: '',
  studyHours: 40,
  studyTarget: 40,
  originBranch: '',
  transferInDate: '',
  transferOutDate: '',
  transferOutBranch: '',
  isFrontline: true,
  isTechnicalTalent: true,
  isDualCultivate: false
})

function openAddMemberDialog() {
  isEditingMember.value = false
  editingMemberId.value = null
  const defaultBranch = isBranchAdmin.value ? currentBranchNameLocked.value : '中共红河红数信息技术服务有限公司支部委员会'
  newMemberForm.value = {
    name: '',
    workNo: 'HH-' + (Math.floor(Math.random() * 890 + 100)),
    idCard: '',
    gender: '男',
    age: 30,
    branchName: defaultBranch,
    deptName: '',
    jobTitle: '',
    partyStatus: 1,
    partyPost: '普通党员',
    partyStandingYears: 0,
    joinPartyDate: '2023-06-20',
    officialPartyDate: '2024-06-20',
    nationalCode: '53250100' + Math.floor(Math.random() * 89999999 + 10000000),
    studyHours: 40,
    studyTarget: 40,
    originBranch: '',
    transferInDate: new Date().toISOString().slice(0, 10),
    transferOutDate: '',
    transferOutBranch: '',
    isFrontline: true,
    isTechnicalTalent: true,
    isDualCultivate: false
  }
  resetEmployment(newMemberForm.value)
  addMemberDialogVisible.value = true
}

function openEditMemberDialog(row) {
  isEditingMember.value = true
  editingMemberId.value = row.id
  newMemberForm.value = {
    name: row.name,
    workNo: row.workNo,
    idCard: row.idCard || '',
    gender: row.gender,
    age: row.age,
    branchName: row.branchName,
    deptName: row.deptName,
    jobTitle: row.jobTitle,
    partyStatus: row.partyStatus,
    partyPost: row.partyPost,
    partyStandingYears: calculatePartyStandingYears(row.joinPartyDate),
    joinPartyDate: row.joinPartyDate || '',
    officialPartyDate: row.officialPartyDate || '',
    nationalCode: row.nationalCode || '',
    studyHours: row.studyHours || 40,
    studyTarget: 40,
    originBranch: row.originBranch || '',
    transferInDate: row.transferInDate || '',
    transferOutDate: row.transferOutDate || '',
    transferOutBranch: row.transferOutBranch || '',
    isFrontline: !!row.isFrontline,
    isTechnicalTalent: !!row.isTechnicalTalent,
    isDualCultivate: !!row.isDualCultivate
  }
  addMemberDialogVisible.value = true
}

function submitAddMember() {
  if (!newMemberForm.value.name.trim()) {
    ElMessage.warning('请输入党员姓名！')
    return
  }
  if (!newMemberForm.value.workNo.trim()) {
    ElMessage.warning('请输入员工工号！')
    return
  }
  if (!newMemberForm.value.idCard.trim() || newMemberForm.value.idCard.length !== 18) {
    ElMessage.warning('请输入规范的 18 位身份证号码！')
    return
  }

  // 根据入党时间自动核算党龄
  if (!validateEmployment(newMemberForm.value)) return
  const autoStandingYears = calculatePartyStandingYears(newMemberForm.value.joinPartyDate)

  if (isEditingMember.value) {
    // 修改已有党员档案
    const idx = rosterList.value.findIndex(m => m.id === editingMemberId.value)
    if (idx !== -1) {
      const item = rosterList.value[idx]
      item.name = newMemberForm.value.name.trim()
      item.idCard = newMemberForm.value.idCard.trim()
      item.gender = newMemberForm.value.gender
      item.age = newMemberForm.value.age
      item.branchName = newMemberForm.value.branchName
      item.deptName = newMemberForm.value.deptName
      item.jobTitle = newMemberForm.value.jobTitle.trim()
      item.partyStatus = newMemberForm.value.partyStatus
      item.partyPost = newMemberForm.value.partyPost
      item.partyStandingYears = autoStandingYears
      item.joinPartyDate = newMemberForm.value.joinPartyDate || null
      item.officialPartyDate = newMemberForm.value.officialPartyDate || null
      item.studyHours = newMemberForm.value.studyHours || 40
      item.studyTarget = 40
      item.originBranch = newMemberForm.value.originBranch || ''
      item.transferInDate = newMemberForm.value.transferInDate || null
      item.transferOutDate = newMemberForm.value.transferOutDate || null
      item.transferOutBranch = newMemberForm.value.transferOutBranch || ''
      item.isFrontline = newMemberForm.value.isFrontline
      item.isTechnicalTalent = newMemberForm.value.isTechnicalTalent
      item.isDualCultivate = newMemberForm.value.isDualCultivate
      const developingMember = membersList.value.find(member => member.id === item.id)
      if (developingMember) {
        developingMember.branchName = item.branchName
        developingMember.branchId = getCompanyByBranch(item.branchName).orgId
        developingMember.deptName = item.deptName
        developingMember.jobTitle = item.jobTitle
      }
    }
    addMemberDialogVisible.value = false
    ElMessage.success(`党员【${newMemberForm.value.name}】档案信息已成功修改更新！`)
  } else {
    // 新增录入
    const exists = rosterList.value.some(m => m.workNo === newMemberForm.value.workNo.trim())
    if (exists) {
      ElMessage.error(`员工工号 [${newMemberForm.value.workNo}] 已存在，请勿重复添加！`)
      return
    }

    const newMemObj = {
      id: Date.now(),
      name: newMemberForm.value.name.trim(),
      workNo: newMemberForm.value.workNo.trim(),
      idCard: newMemberForm.value.idCard.trim(),
      gender: newMemberForm.value.gender,
      age: newMemberForm.value.age,
      branchName: newMemberForm.value.branchName,
      deptName: newMemberForm.value.deptName,
      jobTitle: newMemberForm.value.jobTitle.trim(),
      partyStatus: newMemberForm.value.partyStatus,
      partyPost: newMemberForm.value.partyPost,
      partyStandingYears: autoStandingYears,
      joinPartyDate: newMemberForm.value.joinPartyDate || null,
      officialPartyDate: newMemberForm.value.officialPartyDate || null,
      duesStatus: 1,
      nationalCode: newMemberForm.value.nationalCode,
      studyHours: newMemberForm.value.studyHours || 40,
      studyTarget: 40,
      originBranch: newMemberForm.value.originBranch || '',
      transferInDate: newMemberForm.value.transferInDate || new Date().toISOString().slice(0, 10),
      transferOutDate: newMemberForm.value.transferOutDate || null,
      transferOutBranch: newMemberForm.value.transferOutBranch || '',
      isFrontline: newMemberForm.value.isFrontline,
      isTechnicalTalent: newMemberForm.value.isTechnicalTalent,
      isDualCultivate: newMemberForm.value.isDualCultivate
    }

    rosterList.value.unshift(newMemObj)

    // 自动为录入的党员开通系统个人账号 (工号登录，弱密123456，首次登录强制改密)
    const existsUser = sysUsers.value.some(u => u.workNo === newMemObj.workNo)
    if (!existsUser) {
      sysUsers.value.unshift({
        id: Date.now() + 20,
        username: newMemObj.workNo.toLowerCase(),
        password: '123456',
        mustChangePwd: true,
        realName: newMemObj.name,
        workNo: newMemObj.workNo,
        phone: '139' + Math.floor(Math.random() * 89999999 + 10000000),
        email: `${newMemObj.workNo.toLowerCase()}@honghe-data.com`,
        orgId: getCompanyByBranch(newMemObj.branchName).orgId,
        orgName: newMemObj.branchName,
        roleCode: 'PARTY_MEMBER',
        roleName: '普通在册党员 / 发展成员本人',
        status: 1,
        lastLoginTime: '未登录',
        createdAt: new Date().toISOString().slice(0, 10)
      })
    }

    addMemberDialogVisible.value = false
    ElMessage.success(`党员【${newMemberForm.value.name}】已成功录入花名册！已自动为其开通系统账号（工号：${newMemberForm.value.workNo}，初始密码 123456，首次登录须改密）。`)
  }
}

// 批量导入逻辑
const importRosterDialogVisible = ref(false)
const importActiveStep = ref(1)
const parsedRosterPreviewList = ref([])

function openImportRosterDialog() {
  importActiveStep.value = 1
  parsedRosterPreviewList.value = []
  importRosterDialogVisible.value = true
}

function downloadRosterTemplateExcel() {
  ElMessage.success('已下载《红河数据产业集团_党员花名册导入模板.xlsx》！')
  importActiveStep.value = 2
}

function handleRosterExcelSelected(file) {
  importActiveStep.value = 3
  parsedRosterPreviewList.value = [
    {
      id: Date.now() + 1,
      name: '何明华',
      workNo: 'HH-HS-042',
      gender: '男',
      age: 36,
      branchName: '中共红河红数信息技术服务有限公司支部委员会',
      deptName: '红河红数信息技术服务有限公司 · 技术部',
      jobTitle: '信息安全主管工程师',
      partyStatus: 1,
      partyPost: '党员',
      partyStandingYears: 8,
      joinPartyDate: '2017-06-25',
      officialPartyDate: '2018-06-25',
      duesStatus: 1,
      nationalCode: '532501001989062501',
      isFrontline: true,
      isTechnicalTalent: true,
      isDualCultivate: true
    },
    {
      id: Date.now() + 2,
      name: '许青青',
      workNo: 'HH-MC-058',
      gender: '女',
      age: 30,
      branchName: '中共云南幂次科技有限公司支部委员会',
      deptName: '云南幂次科技有限公司 · 技术创新部',
      jobTitle: '软件研发工程师',
      partyStatus: 1,
      partyPost: '党员',
      partyStandingYears: 4,
      joinPartyDate: '2021-07-01',
      officialPartyDate: '2022-07-01',
      duesStatus: 1,
      nationalCode: '532501001995070102',
      isFrontline: true,
      isTechnicalTalent: true,
      isDualCultivate: false
    },
    {
      id: Date.now() + 3,
      name: '段建军',
      workNo: 'HH-LD-023',
      gender: '男',
      age: 33,
      branchName: '中共红河链达科技有限公司支部委员会',
      deptName: '红河链达科技有限公司 · 供应链管理部',
      jobTitle: '供应链管理专员',
      partyStatus: 2,
      partyPost: '预备党员',
      partyStandingYears: 0,
      joinPartyDate: '2024-10-18',
      officialPartyDate: null,
      duesStatus: 1,
      nationalCode: '532501001992101803',
      isFrontline: true,
      isTechnicalTalent: true,
      isDualCultivate: true
    },
    {
      id: Date.now() + 4,
      name: '杨丽萍',
      workNo: 'HH-JT-018',
      gender: '女',
      age: 27,
      branchName: '中共红河数据产业集团有限公司总支部委员会',
      deptName: '红河数据产业集团有限公司 · 综合管理部',
      jobTitle: '党务人事主管',
      partyStatus: 1,
      partyPost: '党员',
      partyStandingYears: 3,
      joinPartyDate: '2022-05-12',
      officialPartyDate: '2023-05-12',
      duesStatus: 1,
      nationalCode: '532501001998051204',
      isFrontline: false,
      isTechnicalTalent: false,
      isDualCultivate: false
    }
  ]
  ElMessage.success(`已成功解析《${file.name}》，成功校验 4 条合规党员数据！`)
}

function confirmBatchImportRoster() {
  if (parsedRosterPreviewList.value.length === 0) return
  const count = parsedRosterPreviewList.value.length
  rosterList.value.unshift(...parsedRosterPreviewList.value)

  // 批量导入自动开户
  parsedRosterPreviewList.value.forEach((m, idx) => {
    const exists = sysUsers.value.some(u => u.workNo === m.workNo)
    if (!exists) {
      sysUsers.value.push({
        id: Date.now() + 100 + idx,
        username: m.workNo.toLowerCase(),
        password: '123456',
        mustChangePwd: true, // 初始弱密，强制首次改密
        realName: m.name,
        workNo: m.workNo,
        phone: '139' + Math.floor(Math.random() * 89999999 + 10000000),
        email: `${m.workNo.toLowerCase()}@honghe-data.com`,
        orgId: getCompanyByBranch(m.branchName).orgId,
        orgName: m.branchName,
        roleCode: 'PARTY_MEMBER',
        roleName: '普通在册党员 / 发展成员本人',
        status: 1,
        lastLoginTime: '未登录',
        createdAt: new Date().toISOString().slice(0, 10)
      })
    }
  })

  parsedRosterPreviewList.value = []
  importRosterDialogVisible.value = false
  ElMessage.success(`批量导入成功！已将 ${count} 位党员录入花名册并自动开通个人登录账号（工号为账号，初始弱密 123456，首次登录强制改密）。`)
}

// ==========================================
// 2.5 党员转接及调整备案业务逻辑 (转入/转出联动花名册 + 职务调整备案联动职务)
// ==========================================
const transferActiveSubTab = ref('transfer') // 'transfer' | 'adjustment'

// --- 子模块 1: 党员组织关系转接记录 ---
const transfersList = ref([...MOCK_RELATION_TRANSFERS])
const transferSearchKeyword = ref('')
const transferTypeFilter = ref('')
const transferBranchFilter = ref('')

const filteredTransfersList = computed(() => {
  return transfersList.value.filter(t => {
    // 四级数据范围限制：支部管理员仅查看涉及本支部的转接记录
    if (currentRole.value === 'branch_admin_hs') {
      const isHs = (t.fromOrgName && t.fromOrgName.includes('红数')) || (t.toOrgName && t.toOrgName.includes('红数'))
      if (!isHs) return false
    }
    if (currentRole.value === 'branch_admin_mc') {
      const isMc = (t.fromOrgName && t.fromOrgName.includes('幂次')) || (t.toOrgName && t.toOrgName.includes('幂次'))
      if (!isMc) return false
    }
    if (currentRole.value === 'branch_admin_ld') {
      const isLd = (t.fromOrgName && t.fromOrgName.includes('链达')) || (t.toOrgName && t.toOrgName.includes('链达'))
      if (!isLd) return false
    }
    if (currentRole.value === 'party_member' && t.workNo !== (currentUser.value?.workNo || 'HH-HS-012')) {
      return false
    }

    if (transferTypeFilter.value !== '' && t.transferType !== transferTypeFilter.value) return false
    if (transferBranchFilter.value) {
      const hitBranch = (t.fromOrgName && t.fromOrgName === transferBranchFilter.value) || 
                        (t.toOrgName && t.toOrgName === transferBranchFilter.value)
      if (!hitBranch) return false
    }
    if (transferSearchKeyword.value) {
      const kw = transferSearchKeyword.value.toLowerCase()
      const matchName = (t.memberName || '').toLowerCase().includes(kw)
      const matchNo = (t.workNo || '').toLowerCase().includes(kw)
      const matchLetter = (t.letterNo || '').toLowerCase().includes(kw)
      const matchFrom = (t.fromOrgName || '').toLowerCase().includes(kw)
      const matchTo = (t.toOrgName || '').toLowerCase().includes(kw)
      if (!matchName && !matchNo && !matchLetter && !matchFrom && !matchTo) return false
    }
    return true
  })
})

const transferInCount = computed(() => transfersList.value.filter(t => t.transferType === 1).length)
const transferOutCount = computed(() => transfersList.value.filter(t => t.transferType === 2).length)

function resetTransferFilters() {
  transferSearchKeyword.value = ''
  transferTypeFilter.value = ''
  transferBranchFilter.value = ''
}

function exportTransferExcel() {
  ElMessage.success(`已成功导出【中国共产党党员组织关系转接工作台账】(共 ${transfersList.value.length} 条记录，.xlsx 格式)`)
}

// 办理组织关系转入弹窗
const transferInDialogVisible = ref(false)
const transferInForm = ref({
  memberName: '',
  workNo: '',
  idCard: '',
  gender: '男',
  partyStatus: 1,
  fromOrgName: '',
  toOrgName: '中共红河红数信息技术服务有限公司支部委员会',
  deptName: '红河红数信息技术服务有限公司 · 技术部',
  jobTitle: '高级工程师',
  partyPost: '普通党员',
  letterNo: '',
  transferDate: new Date().toISOString().slice(0, 10),
  duesPaidToDate: new Date().toISOString().slice(0, 7),
  phone: '',
  transferReason: '高层次大数据专业技术人才引进调入'
})

function openTransferInDialog() {
  const defaultBranch = isBranchAdmin.value ? currentBranchNameLocked.value : '中共红河红数信息技术服务有限公司支部委员会'
  const dept = getDepartmentOptions(defaultBranch)[0]?.value || ''
  transferInForm.value = {
    memberName: '',
    workNo: 'HH-' + (defaultBranch.includes('红数') ? 'HS' : defaultBranch.includes('幂次') ? 'MC' : defaultBranch.includes('链达') ? 'LD' : 'JT') + '-' + String(Math.floor(Math.random() * 800 + 100)),
    idCard: '',
    gender: '男',
    partyStatus: 1,
    fromOrgName: '',
    toOrgName: defaultBranch,
    deptName: dept,
    jobTitle: '技术骨干 / 业务主管',
    partyPost: '普通党员',
    letterNo: `红数转字〔${new Date().getFullYear()}〕第0${transfersList.value.length + 1}号`,
    transferDate: new Date().toISOString().slice(0, 10),
    duesPaidToDate: new Date().toISOString().slice(0, 7),
    phone: '',
    transferReason: '专业技术骨干选拔调入'
  }
  transferInDialogVisible.value = true
}

function suggestTransferInEmployment() {
  const depts = getDepartmentOptions(transferInForm.value.toOrgName)
  if (depts && depts.length > 0) {
    transferInForm.value.deptName = depts[0].value
    transferInForm.value.jobTitle = depts[0].jobTitle || '专员'
  }
}

function submitTransferIn() {
  if (!transferInForm.value.memberName.trim()) {
    ElMessage.warning('请输入转入党员姓名！')
    return
  }
  if (!transferInForm.value.workNo.trim()) {
    ElMessage.warning('请输入员工工号！')
    return
  }
  if (!transferInForm.value.idCard.trim() || transferInForm.value.idCard.length !== 18) {
    ElMessage.warning('请输入规范的 18 位公民身份证号！')
    return
  }
  if (!transferInForm.value.fromOrgName.trim()) {
    ElMessage.warning('请输入原所在党组织！')
    return
  }
  if (!transferInForm.value.letterNo.trim()) {
    ElMessage.warning('请输入介绍信凭证编号！')
    return
  }

  // 1. 生成转接记录
  const newTransferRecord = {
    id: Date.now(),
    memberId: Date.now(),
    memberName: transferInForm.value.memberName.trim(),
    workNo: transferInForm.value.workNo.trim(),
    idCard: transferInForm.value.idCard.trim(),
    gender: transferInForm.value.gender,
    phone: transferInForm.value.phone || '1398730' + Math.floor(Math.random() * 8999 + 1000),
    partyStatus: transferInForm.value.partyStatus,
    partyPost: transferInForm.value.partyPost || '普通党员',
    transferType: 1, // 转入
    fromOrgId: null,
    fromOrgName: transferInForm.value.fromOrgName.trim(),
    toOrgId: getCompanyByBranch(transferInForm.value.toOrgName).orgId,
    toOrgName: transferInForm.value.toOrgName,
    letterNo: transferInForm.value.letterNo.trim(),
    transferDate: transferInForm.value.transferDate,
    transferReason: transferInForm.value.transferReason.trim() || '组织关系接转调入',
    duesPaidToDate: transferInForm.value.duesPaidToDate,
    operatorName: currentUser.value?.realName || '杨海',
    approvalStatus: 2,
    remark: '介绍信查验无误，已自动同步写入全集团花名册',
    createdAt: new Date().toISOString()
  }
  transfersList.value.unshift(newTransferRecord)

  // 2. 联动花名册：自动建档或恢复在册状态
  const existingRosterIndex = rosterList.value.findIndex(m => m.workNo === transferInForm.value.workNo.trim())
  if (existingRosterIndex !== -1) {
    // 恢复在册
    const m = rosterList.value[existingRosterIndex]
    m.branchName = transferInForm.value.toOrgName
    m.deptName = transferInForm.value.deptName
    m.jobTitle = transferInForm.value.jobTitle
    m.partyStatus = transferInForm.value.partyStatus
    m.partyPost = transferInForm.value.partyPost || '普通党员'
  } else {
    // 全新录入花名册
    rosterList.value.unshift({
      id: Date.now(),
      name: transferInForm.value.memberName.trim(),
      workNo: transferInForm.value.workNo.trim(),
      idCard: transferInForm.value.idCard.trim(),
      gender: transferInForm.value.gender,
      age: 32,
      branchName: transferInForm.value.toOrgName,
      deptName: transferInForm.value.deptName,
      jobTitle: transferInForm.value.jobTitle,
      partyStatus: transferInForm.value.partyStatus,
      partyPost: transferInForm.value.partyPost || '普通党员',
      partyStandingYears: 3,
      joinPartyDate: '2022-07-01',
      officialPartyDate: transferInForm.value.partyStatus === 1 ? '2023-07-01' : null,
      studyHours: 40,
      studyTarget: 40,
      duesStatus: 1,
      nationalCode: '532501' + transferInForm.value.idCard.slice(6, 14) + '01',
      isFrontline: true,
      isTechnicalTalent: true,
      isDualCultivate: false
    })
  }

  // 3. 自动同步为系统用户
  const existsUser = sysUsers.value.some(u => u.workNo === transferInForm.value.workNo.trim())
  if (!existsUser) {
    sysUsers.value.push({
      id: Date.now() + 50,
      username: transferInForm.value.workNo.trim().toLowerCase(),
      password: '123456',
      mustChangePwd: true,
      realName: transferInForm.value.memberName.trim(),
      workNo: transferInForm.value.workNo.trim(),
      phone: transferInForm.value.phone || '13987308888',
      email: `${transferInForm.value.workNo.trim().toLowerCase()}@honghe-data.com`,
      orgId: getCompanyByBranch(transferInForm.value.toOrgName).orgId,
      orgName: transferInForm.value.toOrgName,
      roleCode: 'PARTY_MEMBER',
      roleName: '普通在册党员 / 发展成员本人',
      status: 1,
      lastLoginTime: '未登录',
      createdAt: new Date().toISOString().slice(0, 10)
    })
  }

  transferInDialogVisible.value = false
  ElMessage.success(`党员【${transferInForm.value.memberName}】组织关系转入手续已办结，已成功同步建档至【全集团所有党员花名册】！`)
}

// 办理组织关系转出弹窗
const transferOutDialogVisible = ref(false)
const transferOutForm = ref({
  selectedWorkNo: '',
  memberId: null,
  memberName: '',
  workNo: '',
  idCard: '',
  partyStatus: 1,
  partyPost: '',
  fromOrgName: '',
  toOrgName: '',
  letterNo: '',
  transferDate: new Date().toISOString().slice(0, 10),
  duesPaidToDate: new Date().toISOString().slice(0, 7),
  operatorName: '',
  transferReason: ''
})

function openTransferOutDialog() {
  transferOutForm.value = {
    selectedWorkNo: '',
    memberId: null,
    memberName: '',
    workNo: '',
    idCard: '',
    partyStatus: 1,
    partyPost: '',
    fromOrgName: '',
    toOrgName: '',
    letterNo: `红数转字〔${new Date().getFullYear()}〕第0${transfersList.value.length + 1}号`,
    transferDate: new Date().toISOString().slice(0, 10),
    duesPaidToDate: new Date().toISOString().slice(0, 7),
    operatorName: currentUser.value?.realName || '杨海',
    transferReason: '因个人工作调动辞职离职，组织关系转往新接收单位'
  }
  transferOutDialogVisible.value = true
}

function handleSelectTransferOutMember(workNo) {
  const member = rosterList.value.find(m => m.workNo === workNo)
  if (member) {
    transferOutForm.value.memberId = member.id
    transferOutForm.value.memberName = member.name
    transferOutForm.value.workNo = member.workNo
    transferOutForm.value.idCard = member.idCard || '532501199001010000'
    transferOutForm.value.partyStatus = member.partyStatus
    transferOutForm.value.partyPost = member.partyPost || '普通党员'
    transferOutForm.value.fromOrgName = member.branchName
  }
}

function submitTransferOut() {
  if (!transferOutForm.value.selectedWorkNo) {
    ElMessage.warning('请选择需要转出的党员！')
    return
  }
  if (!transferOutForm.value.toOrgName.trim()) {
    ElMessage.warning('请输入拟转往接收党组织！')
    return
  }
  if (!transferOutForm.value.letterNo.trim()) {
    ElMessage.warning('请输入介绍信存根编号！')
    return
  }
  if (!transferOutForm.value.transferReason.trim()) {
    ElMessage.warning('请输入转出事由！')
    return
  }

  ElMessageBox.confirm(
    `办理组织关系转出后，党员【${transferOutForm.value.memberName}】将依法依规从【全集团所有党员花名册】中除名注销，流转至转接历史归档。确认办理除名转出？`,
    '确认办理组织关系转出',
    {
      confirmButtonText: '确认转出并除名',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 1. 生成转出记录
    const newTransferRecord = {
      id: Date.now(),
      memberId: transferOutForm.value.memberId || Date.now(),
      memberName: transferOutForm.value.memberName,
      workNo: transferOutForm.value.workNo,
      idCard: transferOutForm.value.idCard,
      gender: '男',
      phone: '13987309999',
      partyStatus: transferOutForm.value.partyStatus,
      partyPost: transferOutForm.value.partyPost || '普通党员',
      transferType: 2, // 转出
      fromOrgId: getCompanyByBranch(transferOutForm.value.fromOrgName).orgId,
      fromOrgName: transferOutForm.value.fromOrgName,
      toOrgId: null,
      toOrgName: transferOutForm.value.toOrgName.trim(),
      letterNo: transferOutForm.value.letterNo.trim(),
      transferDate: transferOutForm.value.transferDate,
      transferReason: transferOutForm.value.transferReason.trim(),
      duesPaidToDate: transferOutForm.value.duesPaidToDate,
      operatorName: transferOutForm.value.operatorName || currentUser.value?.realName || '杨海',
      approvalStatus: 2,
      remark: '党费已结清，组织关系凭证已发出，已自花名册除名注销',
      createdAt: new Date().toISOString()
    }
    transfersList.value.unshift(newTransferRecord)

    // 2. 联动花名册：从 rosterList 中除名移除
    const rosterIdx = rosterList.value.findIndex(m => m.workNo === transferOutForm.value.workNo)
    if (rosterIdx !== -1) {
      rosterList.value.splice(rosterIdx, 1)
    }

    transferOutDialogVisible.value = false
    ElMessage.success(`党员【${transferOutForm.value.memberName}】组织关系转出办结，已正式从【全集团所有党员花名册】中除名注销并归档！`)
  }).catch(() => {})
}

// 凭证详情弹窗
const viewTransferDialogVisible = ref(false)
const currentTransferView = ref(null)

function viewTransferRecord(row) {
  currentTransferView.value = { ...row }
  viewTransferDialogVisible.value = true
}

function printTransferCert() {
  ElMessage.success('已连接打印终端，正在打印《中国共产党党员组织关系介绍信存根与转接凭证》！')
}

// --- 子模块 2: 党员党内职务调整备案 ---
const adjustmentsList = ref([...MOCK_POSITION_ADJUSTMENTS])
const adjustmentSearchKeyword = ref('')
const adjustmentBranchFilter = ref('')
const adjustmentPostFilter = ref('')

const filteredAdjustmentsList = computed(() => {
  return adjustmentsList.value.filter(a => {
    // 权限范围
    if (currentRole.value === 'branch_admin_hs' && !a.orgName.includes('红数')) return false
    if (currentRole.value === 'branch_admin_mc' && !a.orgName.includes('幂次')) return false
    if (currentRole.value === 'branch_admin_ld' && !a.orgName.includes('链达')) return false
    if (currentRole.value === 'party_member' && a.workNo !== (currentUser.value?.workNo || 'HH-HS-012')) return false

    if (adjustmentBranchFilter.value && a.orgName !== adjustmentBranchFilter.value) return false
    if (adjustmentPostFilter.value && a.newPost !== adjustmentPostFilter.value) return false
    if (adjustmentSearchKeyword.value) {
      const kw = adjustmentSearchKeyword.value.toLowerCase()
      const matchName = (a.memberName || '').toLowerCase().includes(kw)
      const matchNo = (a.workNo || '').toLowerCase().includes(kw)
      const matchDoc = (a.documentNo || '').toLowerCase().includes(kw)
      const matchPost = (a.newPost || '').toLowerCase().includes(kw)
      const matchOld = (a.oldPost || '').toLowerCase().includes(kw)
      if (!matchName && !matchNo && !matchDoc && !matchPost && !matchOld) return false
    }
    return true
  })
})

const secretaryCount = computed(() => adjustmentsList.value.filter(a => a.newPost && a.newPost.includes('书记')).length)
const committeeCount = computed(() => adjustmentsList.value.filter(a => a.newPost && a.newPost.includes('委员')).length)

function resetAdjustmentFilters() {
  adjustmentSearchKeyword.value = ''
  adjustmentBranchFilter.value = ''
  adjustmentPostFilter.value = ''
}

function exportAdjustmentExcel() {
  ElMessage.success(`已成功导出【中共红河数据产业集团党内职务调整任免备案台账】(共 ${adjustmentsList.value.length} 条记录，.xlsx 格式)`)
}

// 新增职务调整备案弹窗
const adjustmentDialogVisible = ref(false)
const adjustmentForm = ref({
  selectedWorkNo: '',
  memberId: null,
  memberName: '',
  workNo: '',
  orgName: '',
  oldPost: '',
  newPost: '',
  adjustType: '任职任命',
  documentNo: '',
  effectiveDate: new Date().toISOString().slice(0, 10),
  approvalUnit: '中共红河数据产业集团有限公司总支部委员会',
  dutyDescription: ''
})

function openAdjustmentDialog() {
  adjustmentForm.value = {
    selectedWorkNo: '',
    memberId: null,
    memberName: '',
    workNo: '',
    orgName: '',
    oldPost: '',
    newPost: '党支部书记',
    adjustType: '任职任命',
    documentNo: `红数党总任〔${new Date().getFullYear()}〕第0${adjustmentsList.value.length + 1}号`,
    effectiveDate: new Date().toISOString().slice(0, 10),
    approvalUnit: '中共红河数据产业集团有限公司总支部委员会',
    dutyDescription: '主持党支部班子全面工作，统筹抓好基层党组织政治建设与生产经营业务融合'
  }
  adjustmentDialogVisible.value = true
}

function handleSelectAdjustmentMember(workNo) {
  const m = rosterList.value.find(item => item.workNo === workNo)
  if (m) {
    adjustmentForm.value.memberId = m.id
    adjustmentForm.value.memberName = m.name
    adjustmentForm.value.workNo = m.workNo
    adjustmentForm.value.orgName = m.branchName
    adjustmentForm.value.oldPost = m.partyPost || '普通党员'
  }
}

function submitAdjustment() {
  if (!adjustmentForm.value.selectedWorkNo) {
    ElMessage.warning('请选择任职党员！')
    return
  }
  if (!adjustmentForm.value.newPost) {
    ElMessage.warning('请选择调整后新任党内职务！')
    return
  }
  if (!adjustmentForm.value.documentNo.trim()) {
    ElMessage.warning('请输入批复/批文编号！')
    return
  }
  if (!adjustmentForm.value.approvalUnit.trim()) {
    ElMessage.warning('请输入批准机关/决定单位！')
    return
  }

  // 1. 生成调整记录
  const newAdjustmentRecord = {
    id: Date.now(),
    memberId: adjustmentForm.value.memberId || Date.now(),
    memberName: adjustmentForm.value.memberName,
    workNo: adjustmentForm.value.workNo,
    orgId: getCompanyByBranch(adjustmentForm.value.orgName).orgId,
    orgName: adjustmentForm.value.orgName,
    oldPost: adjustmentForm.value.oldPost || '普通党员',
    newPost: adjustmentForm.value.newPost,
    adjustType: adjustmentForm.value.adjustType,
    documentNo: adjustmentForm.value.documentNo.trim(),
    effectiveDate: adjustmentForm.value.effectiveDate,
    approvalUnit: adjustmentForm.value.approvalUnit.trim(),
    dutyDescription: adjustmentForm.value.dutyDescription.trim(),
    operatorName: currentUser.value?.realName || '杨海',
    remark: '调整批文备案归档，花名册职务已自动更新',
    createdAt: new Date().toISOString()
  }
  adjustmentsList.value.unshift(newAdjustmentRecord)

  // 2. 联动花名册：自动更新党内职务
  const rosterItem = rosterList.value.find(m => m.workNo === adjustmentForm.value.workNo)
  if (rosterItem) {
    rosterItem.partyPost = adjustmentForm.value.newPost
  }

  adjustmentDialogVisible.value = false
  ElMessage.success(`党员【${adjustmentForm.value.memberName}】党内职务调整备案成功（文号：${adjustmentForm.value.documentNo}），已自动同步更新花名册党内职务为【${adjustmentForm.value.newPost}】！`)
}

// 职务调整备案表详情
const viewAdjustmentDialogVisible = ref(false)
const currentAdjustmentView = ref(null)

function viewAdjustmentRecord(row) {
  currentAdjustmentView.value = { ...row }
  viewAdjustmentDialogVisible.value = true
}

function printAdjustmentCert() {
  ElMessage.success('已连接打印终端，正在打印《党员领导干部党内职务调整任免备案登记表》！')
}

// ==========================================
// 组织生活“三会一课”与主题党日业务逻辑 (支持单选/多选及彩色议题标签筛选)
// ==========================================
const meetingsList = ref([...MOCK_MEETING_RECORDS])
const meetingKeyword = ref('')
const meetingBranchFilter = ref([]) // 数组：支持单选/多选所属支部
const meetingTypeFilter = ref([]) // 数组：支持单选/多选组织生活类型
const meetingTagFilter = ref([]) // 数组：支持通过议题彩色标签单选/多选筛选

// 议题分类彩色标签库 (支持管理员新增，带颜色标记)
const availableTopicTags = ref([...AVAILABLE_TOPIC_TAGS])

const filteredMeetingsList = computed(() => {
  return meetingsList.value.filter(m => {
    // 四级数据范围限制：支部管理员仅查看本支部三会一课台账
    if (currentRole.value === 'branch_admin_hs' && !m.branchName.includes('红数')) return false
    if (currentRole.value === 'branch_admin_mc' && !m.branchName.includes('幂次')) return false
    if (currentRole.value === 'branch_admin_ld' && !m.branchName.includes('链达')) return false

    // 支部筛选 (单选/多选兼容)
    if (meetingBranchFilter.value && meetingBranchFilter.value.length > 0) {
      if (!meetingBranchFilter.value.includes(m.branchName)) return false
    }
    // 组织生活类型筛选 (单选/多选兼容)
    if (meetingTypeFilter.value && meetingTypeFilter.value.length > 0) {
      if (!meetingTypeFilter.value.includes(m.meetingType)) return false
    }
    // 议题分类标签筛选 (单选/多选兼容)
    if (meetingTagFilter.value && meetingTagFilter.value.length > 0) {
      const rowTags = m.tags || (m.agendaItems ? m.agendaItems.map(a => a.tagName) : [])
      const matched = meetingTagFilter.value.some(selTag => rowTags.includes(selTag))
      if (!matched) return false
    }
    // 关键词综合搜索
    if (meetingKeyword.value) {
      const kw = meetingKeyword.value.toLowerCase()
      const matchTitle = m.title.toLowerCase().includes(kw)
      const matchMod = m.moderator.toLowerCase().includes(kw)
      const matchContent = m.content.toLowerCase().includes(kw)
      const matchAgendas = m.agendaItems ? m.agendaItems.some(a => a.topic.toLowerCase().includes(kw)) : false
      if (!matchTitle && !matchMod && !matchContent && !matchAgendas) return false
    }
    return true
  })
})

function getMeetingTypeTag(type) {
  const map = { 1: 'danger', 2: 'warning', 3: 'primary', 4: 'success' }
  return map[type] || 'info'
}

function resetMeetingFilters() {
  meetingKeyword.value = ''
  meetingBranchFilter.value = []
  meetingTypeFilter.value = []
  meetingTagFilter.value = []
}

function downloadMeetingDoc(row) {
  ElMessage.success(`已生成并下载《${row.docName}》（包含完整出勤签到表与会议纪要原件）`)
}

function exportMeetingsExcel() {
  ElMessage.success('已导出全集团 2025 年度“三会一课”与主题党日规范化开展台账 (.xlsx)')
}

// 管理议题分类标签
const manageTagsDialogVisible = ref(false)
const newTagName = ref('')
const newTagColor = ref('#c21c1d')

function openManageTagsDialog() {
  newTagName.value = ''
  newTagColor.value = '#c21c1d'
  manageTagsDialogVisible.value = true
}

function submitAddNewTag() {
  if (!newTagName.value.trim()) {
    ElMessage.warning('请输入标签名称！')
    return
  }
  const tagId = 'TAG_' + Date.now()
  availableTopicTags.value.push({
    id: tagId,
    name: newTagName.value.trim(),
    color: newTagColor.value,
    desc: '管理员新增自定义议题标签'
  })
  ElMessage.success(`成功新增议题分类标签【${newTagName.value.trim()}】！`)
  newTagName.value = ''
}

function deleteTopicTag(tagId) {
  availableTopicTags.value = availableTopicTags.value.filter(t => t.id !== tagId)
  ElMessage.success('已删除该标签')
}

// 记录与修改组织生活弹窗逻辑
const addMeetingDialogVisible = ref(false)
const isEditingMeeting = ref(false)
const editingMeetingId = ref(null)
const newMeetingForm = ref({
  branchName: '中共红河红数信息技术服务有限公司支部委员会',
  meetingType: 1,
  title: '',
  date: new Date().toISOString().slice(0, 10),
  place: '党员活动室',
  moderator: '李卫民 (支部书记)',
  speaker: '',
  expectedCount: 16,
  actualCount: 16,
  agendaItems: [
    { topic: '', tagId: 'TAG_DEV', tagName: '发展党员', tagColor: '#c21c1d' }
  ],
  content: '',
  relatedStep: 0,
  relatedMember: ''
})

function openAddMeetingDialog() {
  isEditingMeeting.value = false
  editingMeetingId.value = null
  const defaultBranch = isBranchAdmin.value ? currentBranchNameLocked.value : '中共红河红数信息技术服务有限公司支部委员会'
  newMeetingForm.value = {
    branchName: defaultBranch,
    meetingType: 1,
    title: '',
    date: new Date().toISOString().slice(0, 10),
    place: '党员活动室',
    moderator: currentUser.value?.realName ? `${currentUser.value.realName} (支部书记)` : '李卫民 (支部书记)',
    speaker: '',
    expectedCount: 16,
    actualCount: 16,
    agendaItems: [
      { topic: '', tagId: 'TAG_DEV', tagName: '发展党员', tagColor: '#c21c1d' }
    ],
    content: '',
    relatedStep: 0,
    relatedMember: ''
  }
  addMeetingDialogVisible.value = true
}

function deleteMeetingRecord(row) {
  // 权限检查：支部管理员仅能删除本支部的会议记录
  if (isBranchAdmin.value && row.branchName !== currentBranchNameLocked.value) {
    ElMessageBox.alert('您无权删除其他党支部的组织生活记录！', '跨支部越权拦截', { type: 'error' })
    return
  }

  ElMessageBox.confirm(
    `确定要永久删除本次【${row.meetingTypeName}】记录《${row.title}》（召开日期：${row.date}）吗？删除后相关考勤与纪要凭证将被移除。`,
    '删除组织生活记录确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(() => {
    meetingsList.value = meetingsList.value.filter(m => m.id !== row.id)
    ElMessage.success(`会议记录《${row.title}》已成功删除！`)
  }).catch(() => {})
}

function openEditMeetingDialog(row) {
  isEditingMeeting.value = true
  editingMeetingId.value = row.id
  newMeetingForm.value = {
    branchName: row.branchName,
    meetingType: row.meetingType,
    title: row.title,
    date: row.date,
    place: row.place,
    moderator: row.moderator,
    speaker: row.speaker || '',
    expectedCount: row.expectedCount,
    actualCount: row.actualCount,
    agendaItems: row.agendaItems ? JSON.parse(JSON.stringify(row.agendaItems)) : [],
    content: row.content || '',
    relatedStep: row.relatedStep || 0,
    relatedMember: row.relatedMember || ''
  }
  addMeetingDialogVisible.value = true
}

function addAgendaItemRow() {
  newMeetingForm.value.agendaItems.push({
    topic: '',
    tagId: 'TAG_DEV',
    tagName: '发展党员',
    tagColor: '#c21c1d'
  })
}

function removeAgendaItemRow(idx) {
  newMeetingForm.value.agendaItems.splice(idx, 1)
}

function handleAgendaTagChange(agenda, tagId) {
  const tag = availableTopicTags.value.find(t => t.id === tagId)
  if (tag) {
    agenda.tagId = tag.id
    agenda.tagName = tag.name
    agenda.tagColor = tag.color
  }
}

function submitAddMeeting() {
  if (!newMeetingForm.value.title.trim()) {
    ElMessage.warning('请输入会议主要议题！')
    return
  }
  const typeMap = { 1: '支委会', 2: '支部党员大会', 3: '专题党课', 4: '主题党日' }
  const shortMap = {
    '中共红河红数信息技术服务有限公司支部委员会': '红数信息',
    '中共云南幂次科技有限公司支部委员会': '幂次科技',
    '中共红河链达科技有限公司支部委员会': '链达科技',
    '中共红河数据产业集团有限公司总支部委员会': '集团党总支'
  }
  const validAgendas = newMeetingForm.value.agendaItems.filter(a => a.topic && a.topic.trim())

  if (isEditingMeeting.value) {
    // 管理员修改已有会议记录
    const idx = meetingsList.value.findIndex(m => m.id === editingMeetingId.value)
    if (idx !== -1) {
      const item = meetingsList.value[idx]
      item.branchName = newMeetingForm.value.branchName
      item.branchShort = shortMap[newMeetingForm.value.branchName] || '党组织'
      item.meetingType = newMeetingForm.value.meetingType
      item.meetingTypeName = typeMap[newMeetingForm.value.meetingType] || '组织生活'
      item.title = newMeetingForm.value.title.trim()
      item.date = newMeetingForm.value.date
      item.place = newMeetingForm.value.place || '党员活动室'
      item.moderator = newMeetingForm.value.moderator || '支部书记'
      item.speaker = newMeetingForm.value.speaker || ''
      item.expectedCount = newMeetingForm.value.expectedCount
      item.actualCount = newMeetingForm.value.actualCount
      item.attendanceRate = Math.round(newMeetingForm.value.actualCount / newMeetingForm.value.expectedCount * 100)
      item.agendaItems = validAgendas
      item.tags = validAgendas.map(a => a.tagName)
      item.content = newMeetingForm.value.content || ''
      item.relatedStep = newMeetingForm.value.relatedStep || 0
      item.relatedMember = newMeetingForm.value.relatedMember || ''
    }
    addMeetingDialogVisible.value = false
    ElMessage.success('会议记录与决议纪实已成功修改更新！')
  } else {
    // 新增录入
    meetingsList.value.unshift({
      id: Date.now(),
      branchName: newMeetingForm.value.branchName,
      branchShort: shortMap[newMeetingForm.value.branchName] || '党组织',
      meetingType: newMeetingForm.value.meetingType,
      meetingTypeName: typeMap[newMeetingForm.value.meetingType] || '组织生活',
      title: newMeetingForm.value.title.trim(),
      date: newMeetingForm.value.date,
      place: newMeetingForm.value.place || '党员活动室',
      moderator: newMeetingForm.value.moderator || '支部书记',
      speaker: newMeetingForm.value.speaker || '',
      expectedCount: newMeetingForm.value.expectedCount,
      actualCount: newMeetingForm.value.actualCount,
      attendanceRate: Math.round(newMeetingForm.value.actualCount / newMeetingForm.value.expectedCount * 100),
      attendees: '支部在册党员及参会骨干',
      agendaItems: validAgendas,
      tags: validAgendas.map(a => a.tagName),
      content: newMeetingForm.value.content || '按规程召开会议，形成会议决议并归档纪要。',
      docName: `${shortMap[newMeetingForm.value.branchName]}_${newMeetingForm.value.date}_会议纪要.docx`,
      relatedStep: newMeetingForm.value.relatedStep || 0,
      relatedMember: newMeetingForm.value.relatedMember || ''
    })
    addMeetingDialogVisible.value = false
    ElMessage.success('组织生活会议纪要已归档入库！已同步更新三会一课台账与大屏指标。')
  }
}

// 会议全套规范附件管理逻辑 (通知、纪要、决议、签到表、现场照片)
const meetingAttachmentsDialogVisible = ref(false)
const currentMeetingForAttach = ref(null)
const uploadMeetingAttachType = ref('MINUTES')
const tempMeetingAttachFile = ref(null)
const currentMeetingAttachmentsList = ref([])

function openMeetingAttachmentsDialog(row) {
  currentMeetingForAttach.value = row
  uploadMeetingAttachType.value = 'MINUTES'
  tempMeetingAttachFile.value = null
  
  currentMeetingAttachmentsList.value = [
    {
      id: 1,
      attachType: 'NOTICE',
      attachTypeName: '会议通知',
      fileName: `${row.branchShort}_关于召开${row.title}的通知.pdf`,
      fileSize: '156 KB',
      uploadTime: `${row.date} 09:30`
    },
    {
      id: 2,
      attachType: 'MINUTES',
      attachTypeName: '会议纪要',
      fileName: row.docName,
      fileSize: '342 KB',
      uploadTime: `${row.date} 16:45`
    },
    {
      id: 3,
      attachType: 'SIGNIN',
      attachTypeName: '签到考勤表',
      fileName: `${row.branchShort}_${row.date}_到会党员签名表扫描件.pdf`,
      fileSize: '820 KB',
      uploadTime: `${row.date} 17:00`
    }
  ]
  if (row.relatedStep > 0) {
    currentMeetingAttachmentsList.value.push({
      id: 4,
      attachType: 'RESOLUTION',
      attachTypeName: '表决决议书',
      fileName: `${row.branchShort}_关于${row.relatedMember}同志的支部表决决议书.docx`,
      fileSize: '210 KB',
      uploadTime: `${row.date} 17:15`
    })
  }

  meetingAttachmentsDialogVisible.value = true
}

function getAttachCategoryTag(type) {
  const map = { NOTICE: 'info', MINUTES: 'primary', RESOLUTION: 'danger', SIGNIN: 'warning', PHOTO: 'success' }
  return map[type] || 'info'
}

function handleMeetingAttachSelected(file) {
  tempMeetingAttachFile.value = file.name
}

function confirmUploadMeetingAttach() {
  if (!tempMeetingAttachFile.value) {
    ElMessage.warning('请先选择要上传的本地附件文件！')
    return
  }
  const typeMap = { NOTICE: '会议通知', MINUTES: '会议纪要', RESOLUTION: '表决决议书', SIGNIN: '签到考勤表', PHOTO: '现场纪实照片' }
  currentMeetingAttachmentsList.value.unshift({
    id: Date.now(),
    attachType: uploadMeetingAttachType.value,
    attachTypeName: typeMap[uploadMeetingAttachType.value] || '附件材料',
    fileName: tempMeetingAttachFile.value,
    fileSize: '280 KB',
    uploadTime: new Date().toISOString().slice(0, 16).replace('T', ' ')
  })
  tempMeetingAttachFile.value = null
  ElMessage.success(`成功上传并归档《${typeMap[uploadMeetingAttachType.value]}》文件！`)
}

function downloadSingleMeetingAttach(row) {
  ElMessage.success(`正在下载《${row.fileName}》`)
}

function deleteMeetingAttach(row) {
  currentMeetingAttachmentsList.value = currentMeetingAttachmentsList.value.filter(a => a.id !== row.id)
  ElMessage.success(`已删除附件《${row.fileName}》`)
}

// ==========================================
// 3. 25步文书模板管理（双轨制：默认+导入）
// ==========================================
const templatesList = ref([...TEMPLATES_CATALOG_25])
const uploadDialogVisible = ref(false)
const selectedTemplateForUpload = ref(null)
const tempUploadedFileName = ref('')

const customizedTemplatesCount = computed(() => {
  return templatesList.value.filter(t => t.isCustomized).length
})

function downloadCurrentTemplate(tpl) {
  const targetName = tpl.isCustomized ? tpl.customName : tpl.defaultName
  ElMessage.success(`正在下载当前使用版本《${targetName}》`)
}

function downloadDefaultTemplate(tpl) {
  ElMessage.success(`正在下载系统官方标准模板《${tpl.defaultName}》`)
}

function openUploadDialog(tpl) {
  selectedTemplateForUpload.value = tpl
  tempUploadedFileName.value = ''
  uploadDialogVisible.value = true
}

function handleFileSelected(file) {
  tempUploadedFileName.value = file.name
}

function confirmUploadCustomTemplate() {
  if (!tempUploadedFileName.value) {
    ElMessage.warning('请先选择要上传的 .docx 格式模板文件')
    return
  }
  const tpl = selectedTemplateForUpload.value
  tpl.isCustomized = true
  tpl.customName = tempUploadedFileName.value
  tpl.version = 'v' + Math.floor(Math.random() * 9 + 1) + '.0 (企业定制)'
  tpl.updateUser = '当前管理员'
  tpl.updateTime = new Date().toISOString().slice(0, 10)
  
  uploadDialogVisible.value = false
  ElMessage.success(`第 ${tpl.stepId} 步自定义模板导入成功！已生效为全集团套打首选版本。`)
}

function restoreDefaultTemplate(tpl) {
  ElMessageBox.confirm(
    `确定要将第 ${tpl.stepId} 步《${tpl.name}》恢复为系统官方内置标准模板吗？`,
    '恢复默认确认',
    { confirmButtonText: '确认恢复', cancelButtonText: '取消', type: 'warning' }
  ).then(() => {
    tpl.isCustomized = false
    tpl.version = 'v1.0 (系统内置)'
    tpl.customName = ''
    tpl.updateUser = '系统内置'
    tpl.updateTime = '2024-01-01'
    ElMessage.success(`第 ${tpl.stepId} 步已恢复为官方标准版本！`)
  }).catch(() => {})
}

// ==========================================
// 组织与个人奖惩/荣誉台账业务逻辑
// ==========================================
const honorsList = ref([...MOCK_HONOR_PUNISHMENT_LIST])
const honorKeyword = ref('')
const honorCategoryFilter = ref(null)
const honorRecordTypeFilter = ref(null)
const honorBranchFilter = ref([])
const honorLevelFilter = ref('')

const branchLockedMemberRoster = computed(() => {
  if (isBranchAdmin.value) {
    return rosterList.value.filter(m => m.branchName === currentBranchNameLocked.value)
  }
  return rosterList.value
})

const filteredHonorsList = computed(() => {
  return honorsList.value.filter(item => {
    // 四级数据范围限制：支部管理员仅查看本支部荣誉与奖惩
    if (currentRole.value === 'branch_admin_hs' && !item.orgName.includes('红数')) return false
    if (currentRole.value === 'branch_admin_mc' && !item.orgName.includes('幂次')) return false
    if (currentRole.value === 'branch_admin_ld' && !item.orgName.includes('链达')) return false

    if (honorCategoryFilter.value !== null && item.category !== honorCategoryFilter.value) return false
    if (honorRecordTypeFilter.value !== null && item.recordType !== honorRecordTypeFilter.value) return false
    if (honorBranchFilter.value && honorBranchFilter.value.length > 0) {
      if (!honorBranchFilter.value.includes(item.orgName)) return false
    }
    if (honorLevelFilter.value && item.level !== honorLevelFilter.value) return false
    if (honorKeyword.value) {
      const kw = honorKeyword.value.toLowerCase()
      const matchTitle = item.title.toLowerCase().includes(kw)
      const matchTarget = item.targetName.toLowerCase().includes(kw)
      const matchDoc = item.docNo ? item.docNo.toLowerCase().includes(kw) : false
      const matchReason = item.reasonContent ? item.reasonContent.toLowerCase().includes(kw) : false
      if (!matchTitle && !matchTarget && !matchDoc && !matchReason) return false
    }
    return true
  })
})

function getHonorCount(category, recordType) {
  return honorsList.value.filter(item => {
    if (category !== null && item.category !== category) return false
    if (recordType !== null && item.recordType !== recordType) return false
    return true
  }).length
}

function getHonorLevelTag(level) {
  const map = { '国家级': 'danger', '省部级': 'warning', '州级/市级': 'primary', '集团级': 'success', '支部级': 'info' }
  return map[level] || 'info'
}

function resetHonorFilters() {
  honorKeyword.value = ''
  honorCategoryFilter.value = null
  honorRecordTypeFilter.value = null
  honorBranchFilter.value = []
  honorLevelFilter.value = ''
}

function exportHonorsExcel() {
  ElMessage.success(`已导出全集团党组织及党员奖惩/荣誉台账清单 (共 ${honorsList.value.length} 项，.xlsx 格式)`)
}

// 登记与修改奖惩荣誉弹窗
const honorDialogVisible = ref(false)
const isEditingHonor = ref(false)
const editingHonorId = ref(null)
const newHonorForm = ref({
  category: 2,
  recordType: 1,
  targetName: '中共云南幂次科技有限公司支部委员会',
  orgName: '中共云南幂次科技有限公司支部委员会',
  title: '',
  level: '省部级',
  grantOrg: '',
  docNo: '',
  recordDate: new Date().toISOString().slice(0, 10),
  reasonContent: ''
})

function openAddHonorDialog() {
  isEditingHonor.value = false
  editingHonorId.value = null
  const defaultOrg = isBranchAdmin.value ? currentBranchNameLocked.value : '中共红河数据产业集团有限公司总支部委员会'
  newHonorForm.value = {
    category: 2,
    recordType: 1,
    targetName: defaultOrg,
    orgName: defaultOrg,
    title: '',
    level: '集团级',
    grantOrg: '中共红河数据产业集团有限公司总支部委员会',
    docNo: '红数党总发〔2025〕08号',
    recordDate: new Date().toISOString().slice(0, 10),
    reasonContent: ''
  }
  honorDialogVisible.value = true
}

function openEditHonorDialog(row) {
  isEditingHonor.value = true
  editingHonorId.value = row.id
  newHonorForm.value = {
    category: row.category,
    recordType: row.recordType,
    targetName: row.targetName,
    orgName: row.orgName,
    title: row.title,
    level: row.level,
    grantOrg: row.grantOrg || '',
    docNo: row.docNo || '',
    recordDate: row.recordDate,
    reasonContent: row.reasonContent || ''
  }
  honorDialogVisible.value = true
}

function handleHonorCategoryChange(val) {
  if (val === 2) {
    const org = isBranchAdmin.value ? currentBranchNameLocked.value : '中共云南幂次科技有限公司支部委员会'
    newHonorForm.value.targetName = org
    newHonorForm.value.orgName = org
  } else {
    if (isBranchAdmin.value) {
      const myMembers = branchLockedMemberRoster.value
      newHonorForm.value.targetName = myMembers.length ? myMembers[0].name : currentUser.value?.realName
      newHonorForm.value.orgName = currentBranchNameLocked.value
    } else {
      newHonorForm.value.targetName = '朱文华'
      newHonorForm.value.orgName = '中共红河数据产业集团有限公司总支部委员会'
    }
  }
}

function handleOrgTargetChange(val) {
  newHonorForm.value.orgName = val
}

function handleMemberTargetChange(val) {
  const found = rosterList.value.find(m => m.name === val)
  if (found) {
    newHonorForm.value.orgName = found.branchName
  }
}

function submitSaveHonor() {
  if (!newHonorForm.value.title.trim()) {
    ElMessage.warning('请输入奖惩/表彰名称！')
    return
  }
  if (!newHonorForm.value.targetName) {
    ElMessage.warning('请选择或输入获奖/受处分主体！')
    return
  }

  // 支部管理员越权校验：严禁给其他支部申报组织荣誉或处分
  if (isBranchAdmin.value && newHonorForm.value.orgName !== currentBranchNameLocked.value) {
    ElMessageBox.alert('您无权为其他党支部或外支部党员登记表彰处分！', '越权拦截', { type: 'error' })
    return
  }

  const shortMap = {
    '中共红河红数信息技术服务有限公司支部委员会': '红数信息',
    '中共云南幂次科技有限公司支部委员会': '幂次科技',
    '中共红河链达科技有限公司支部委员会': '链达科技',
    '中共红河数据产业集团有限公司总支部委员会': '集团党总支'
  }

  if (isEditingHonor.value) {
    const idx = honorsList.value.findIndex(h => h.id === editingHonorId.value)
    if (idx !== -1) {
      const item = honorsList.value[idx]
      item.category = newHonorForm.value.category
      item.categoryName = newHonorForm.value.category === 2 ? '组织荣誉/奖惩' : '个人荣誉/奖惩'
      item.recordType = newHonorForm.value.recordType
      item.recordTypeName = newHonorForm.value.recordType === 1 ? '荣誉表彰' : '纪律处分/诫勉'
      item.title = newHonorForm.value.title.trim()
      item.targetName = newHonorForm.value.targetName
      item.orgName = newHonorForm.value.orgName
      item.orgShort = shortMap[newHonorForm.value.orgName] || '党组织'
      item.level = newHonorForm.value.level
      item.grantOrg = newHonorForm.value.grantOrg
      item.docNo = newHonorForm.value.docNo
      item.recordDate = newHonorForm.value.recordDate
      item.reasonContent = newHonorForm.value.reasonContent
    }
    honorDialogVisible.value = false
    ElMessage.success('奖惩/荣誉记录已成功修改更新！')
  } else {
    honorsList.value.unshift({
      id: Date.now(),
      category: newHonorForm.value.category,
      categoryName: newHonorForm.value.category === 2 ? '组织荣誉/奖惩' : '个人荣誉/奖惩',
      recordType: newHonorForm.value.recordType,
      recordTypeName: newHonorForm.value.recordType === 1 ? '荣誉表彰' : '纪律处分/诫勉',
      title: newHonorForm.value.title.trim(),
      targetName: newHonorForm.value.targetName,
      orgName: newHonorForm.value.orgName,
      orgShort: shortMap[newHonorForm.value.orgName] || '党组织',
      level: newHonorForm.value.level,
      grantOrg: newHonorForm.value.grantOrg,
      docNo: newHonorForm.value.docNo,
      recordDate: newHonorForm.value.recordDate,
      reasonContent: newHonorForm.value.reasonContent,
      attachmentPath: '规范表彰证明文档.pdf'
    })
    honorDialogVisible.value = false
    ElMessage.success('奖惩/荣誉记录已成功登记入库！')
  }
}

function deleteHonorItem(row) {
  ElMessageBox.confirm(
    `确定要删除《${row.title}》（主体：${row.targetName}）的记录吗？`,
    '删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(() => {
    honorsList.value = honorsList.value.filter(h => h.id !== row.id)
    ElMessage.success('记录已成功删除')
  }).catch(() => {})
}

// ==========================================
// 4. 党建通知中心与多渠道管理
// ==========================================
const emptyNoticeChannels = () => Object.entries(NOTICE_CHANNEL_META).map(([code, meta], index) => ({
  id: index + 1, channelCode: code, channelName: meta.name, icon: meta.icon,
  enabled: 0, configJson: '{}', templateJson: '{}', remark: meta.hint
}))
const noticeChannels = ref(emptyNoticeChannels())
const noticeLogs = ref([])
const noticeRecipients = ref([])
const noticeBusy = ref(false)
const channelForm = ref({})
const smsTemplates = ref({})
const noticeKeyword = ref('')
const filterNoticeType = ref('')
const filterNoticeChannel = ref('')
const filterNoticeRead = ref('')

const unreadNoticeCount = computed(() => {
  return noticeLogs.value.filter(n => n.isRead === 0 && n.sendStatus === 1 && n.receiverId === currentUser.value?.id).length
})

const filteredNoticeLogs = computed(() => {
  return noticeLogs.value.filter(item => {
    if (filterNoticeType.value && item.noticeType !== filterNoticeType.value) return false
    if (filterNoticeChannel.value && item.channelCode !== filterNoticeChannel.value) return false
    if (filterNoticeRead.value !== '' && item.isRead !== filterNoticeRead.value) return false
    if (noticeKeyword.value) {
      const kw = noticeKeyword.value.toLowerCase()
      const matchTitle = item.title.toLowerCase().includes(kw)
      const matchContent = item.content.toLowerCase().includes(kw)
      const matchRec = (item.receiverName || '').toLowerCase().includes(kw)
      if (!matchTitle && !matchContent && !matchRec) return false
    }
    return true
  })
})

const noticeDialogVisible = ref(false)
const newNoticeForm = ref({
  noticeType: 'REGULAR',
  channelCode: 'IN_APP',
  receiverType: 'USER',
  receiverId: null,
  receiverName: '',
  receiverTarget: '',
  title: '',
  content: ''
})

const channelConfigDialogVisible = ref(false)
const currentEditingChannel = ref(null)

const noticeDetailVisible = ref(false)
const currentViewingNotice = ref(null)

async function loadNoticeData() {
  try {
    const [channels, logs] = await Promise.all([
      apiRequest(hasRole('sys_admin') ? '/notice/channels' : '/notice/available-channels'),
      apiRequest('/notice/logs')
    ])
    noticeChannels.value = channels.map(ch => ({ ...ch, icon: NOTICE_CHANNEL_META[ch.channelCode]?.icon || 'Bell' }))
    noticeLogs.value = logs.map(row => ({ ...row, noticeTypeName: NOTICE_TYPES[row.noticeType] || '党建通知',
      typeTag: 'info', channelName: NOTICE_CHANNEL_META[row.channelCode]?.name || row.channelCode }))
  } catch (error) { ElMessage.error(error.message) }
}

watch(apiSession, session => {
  noticeChannels.value = emptyNoticeChannels()
  noticeLogs.value = []
  noticeRecipients.value = []
  if (session) loadNoticeData()
})
watch(activeTab, tab => {
  if (tab === 'notices' && apiSession.value) loadNoticeData()
  if (tab === 'users' && apiSession.value && hasRole('sys_admin')) loadSysUsers()
})

async function handleChannelToggle(ch) {
  if (noticeBusy.value) return
  noticeBusy.value = true
  try {
    await apiRequest(`/notice/channels/${ch.id}`, { method: 'PUT', body: { enabled: ch.enabled } })
    ElMessage.success(ch.enabled === 1 ? '渠道已启用' : '渠道已停用')
  } catch (error) {
    ch.enabled = ch.enabled === 1 ? 0 : 1
    ElMessage.error(error.message)
  } finally { noticeBusy.value = false }
}

function openChannelConfig(ch) {
  currentEditingChannel.value = { ...ch }
  const cfg = JSON.parse(ch.configJson || '{}')
  channelForm.value = Object.fromEntries((NOTICE_CHANNEL_META[ch.channelCode]?.fields || []).map(field => [field.key, cfg[field.key] ?? field.default ?? '']))
  const templates = JSON.parse(ch.templateJson || '{}')
  smsTemplates.value = Object.fromEntries(Object.keys(NOTICE_TYPES).map(type => [type, {
    templateCode: templates[type]?.templateCode || '', mapping: JSON.stringify(templates[type]?.parameters || {})
  }]))
  channelConfigDialogVisible.value = true
}

async function saveChannelConfig() {
  if (noticeBusy.value) return
  noticeBusy.value = true
  try {
    const templates = {}
    if (currentEditingChannel.value.channelCode === 'SMS') {
      for (const [type, item] of Object.entries(smsTemplates.value)) {
        if (item.templateCode.trim()) {
          let parameters
          try { parameters = JSON.parse(item.mapping) } catch { throw new Error(`${NOTICE_TYPES[type]}的变量映射不是有效 JSON`) }
          if (!parameters || Array.isArray(parameters) || typeof parameters !== 'object') throw new Error('模板变量映射须为对象')
          templates[type] = { templateCode: item.templateCode.trim(), parameters }
        }
      }
    }
    await apiRequest(`/notice/channels/${currentEditingChannel.value.id}`, { method: 'PUT', body: {
      configJson: JSON.stringify(channelForm.value), templateJson: JSON.stringify(templates), remark: currentEditingChannel.value.remark
    } })
    channelConfigDialogVisible.value = false
    ElMessage.success('渠道配置已保存到服务器')
    await loadNoticeData()
  } catch (error) { ElMessage.error(error.message) }
  finally { noticeBusy.value = false }
}

function noticeTargetHint(code) {
  return { WECHAT_WORK: '企业微信成员 UserID', DINGTALK: '钉钉成员 UserID（不是邮箱）', SMS: '一个11位中国大陆手机号', EMAIL: '一个有效邮箱地址', IN_APP: '当前登录用户' }[code]
}

async function testChannelPing(ch) {
  if (noticeBusy.value) return
  if (ch.enabled === 0) {
    ElMessage.warning(`渠道【${ch.channelName}】当前处于停用状态，请先启用后再进行联通测试！`)
    return
  }
  try {
    let target = ''
    if (ch.channelCode === 'IN_APP') {
      await ElMessageBox.confirm('将向当前账号发送一条站内测试通知。', '发送测试消息', { confirmButtonText: '发送', cancelButtonText: '取消' })
    } else {
      const result = await ElMessageBox.prompt(`将实际发送测试消息${ch.channelCode === 'SMS' ? '，并产生短信费用' : ''}。请输入${noticeTargetHint(ch.channelCode)}。`, '发送测试消息', {
        confirmButtonText: '确认发送', cancelButtonText: '取消', inputValidator: value => !!value?.trim() || '接收地址不能为空'
      })
      target = result.value.trim()
    }
    noticeBusy.value = true
    await apiRequest(`/notice/channels/${ch.channelCode}/test`, { method: 'POST', body: { target } })
    ElMessage.success('测试消息已受理，请核对实际收件情况')
  } catch (error) { if (error instanceof Error) ElMessage.error(error.message) }
  finally { noticeBusy.value = false; if (apiSession.value) await loadNoticeData() }
}

async function openSendNoticeDialog() {
  try { noticeRecipients.value = await apiRequest('/notice/recipients') }
  catch (error) { ElMessage.error(error.message); return }
  newNoticeForm.value = {
    noticeType: 'REGULAR',
    channelCode: noticeChannels.value.find(ch => ch.enabled === 1)?.channelCode || 'IN_APP',
    receiverType: 'USER',
    receiverId: null,
    receiverName: '',
    receiverTarget: '',
    title: '',
    content: ''
  }
  noticeDialogVisible.value = true
}

async function submitSendNotice() {
  if (noticeBusy.value) return
  if (!newNoticeForm.value.title.trim()) {
    ElMessage.warning('请输入通知标题！')
    return
  }
  if (!newNoticeForm.value.content.trim()) {
    ElMessage.warning('请输入通知正文内容！')
    return
  }

  if (!newNoticeForm.value.receiverId && (newNoticeForm.value.channelCode === 'IN_APP' || !newNoticeForm.value.receiverTarget.trim())) {
    return ElMessage.warning('请选择接收用户或填写有效接收地址')
  }
  noticeBusy.value = true
  try {
    await apiRequest('/notice/send', { method: 'POST', body: { ...newNoticeForm.value, receiverId: newNoticeForm.value.receiverId || null } })
    noticeDialogVisible.value = false
    ElMessage.success('通知已受理，请在记录中核对结果')
  } catch (error) { ElMessage.error(error.message) }
  finally { noticeBusy.value = false; await loadNoticeData() }
}

function viewNoticeDetail(row) {
  currentViewingNotice.value = row
  noticeDetailVisible.value = true
}

async function markNoticeAsRead(row) {
  try {
    await apiRequest(`/notice/logs/${row.id}/read`, { method: 'PUT' })
    row.isRead = 1
    ElMessage.success('已标记本人通知为已读')
  } catch (error) { ElMessage.error(error.message) }
}

async function markAllNoticesRead() {
  try {
    await apiRequest('/notice/logs/read-all', { method: 'POST' })
    await loadNoticeData()
    ElMessage.success('本人通知已全部标记为已读')
  } catch (error) { ElMessage.error(error.message) }
}

async function triggerSystemComplianceScan() {
  if (noticeBusy.value) return
  try {
    await ElMessageBox.confirm('将扫描数据库中的发展档案，并通过已配置渠道向相关人员发送提醒。同类预警每天每人只尝试一次。', '执行扫描并发送', { confirmButtonText: '执行', cancelButtonText: '取消' })
    noticeBusy.value = true
    const result = await apiRequest('/notice/trigger-warnings', { method: 'POST' })
    ElMessage.success(`扫描完成，本次受理 ${result.dispatchedCount} 条；请查看记录中的失败原因`)
  } catch (error) { if (error instanceof Error) ElMessage.error(error.message) }
  finally { noticeBusy.value = false; if (apiSession.value) await loadNoticeData() }
}

// ==========================================
// 5. 党务用户与权限体系 (RBAC)
// ==========================================
// 权限编码用于鉴权，角色卡片展示对应的中文名称。
const permissionLabels = {
  'user:manage': '用户管理',
  'role:manage': '角色权限管理',
  'notice:channel_manage': '通知渠道配置',
  'notice:send': '发送通知',
  'notice:view': '查看通知',
  'workbench:view': '查看发展党员工作台',
  'workbench:create_applicant': '新建入党申请人档案',
  'workbench:advance': '推进发展流程',
  'workbench:audit': '党总支审批',
  'workbench:transfer': '组织关系转接',
  'workbench:export': '导出发展台账',
  'workbench:block_override': '流程拦截处理',
  'roster:view': '查看党员花名册',
  'roster:create': '新增党员档案',
  'roster:edit': '修改党员档案',
  'roster:import': '导入党员花名册',
  'roster:export': '导出党员花名册',
  'meeting:view': '查看组织生活台账',
  'meeting:create': '新增组织生活记录',
  'meeting:edit': '修改组织生活记录',
  'meeting:delete': '删除组织生活记录',
  'meeting:tags_manage': '管理议题标签',
  'meeting:export': '导出组织生活台账',
  'honor:view': '查看奖惩台账',
  'honor:create': '新增奖惩记录',
  'honor:edit': '修改奖惩记录',
  'honor:delete': '删除奖惩记录',
  'honor:export': '导出奖惩台账',
  'template:view': '查看文书模板',
  'template:upload': '上传文书模板',
  'template:reset': '恢复默认模板',
  'cockpit:view': '查看党建驾驶舱',
  'member:self_view': '查看个人档案'
}

const sysRoles = ref([...MOCK_SYS_ROLES])
const sysUsers = ref([...MOCK_SYS_USERS])
async function loadSysUsers() {
  try {
    const [roles, users] = await Promise.all([apiRequest('/auth/roles'), apiRequest('/auth/users')])
    sysRoles.value = roles.map(role => ({ ...role, permissions: JSON.parse(role.permissions || '[]'), userCount: users.filter(user => user.roleIds.includes(role.id)).length }))
    sysUsers.value = users.map(user => {
      const role = roles.find(item => user.roleIds.includes(item.id))
      return { ...user, roleCode: role?.roleCode || '', roleName: user.roleNames.join(' / ') }
    })
  } catch (error) { ElMessage.error(error.message) }
}
const userKeyword = ref('')
const filterUserOrg = ref('')
const filterUserRole = ref('')

const filteredSysUsers = computed(() => {
  return sysUsers.value.filter(u => {
    if (filterUserOrg.value && u.orgName !== filterUserOrg.value) return false
    if (filterUserRole.value && u.roleCode !== filterUserRole.value) return false
    if (userKeyword.value) {
      const kw = userKeyword.value.toLowerCase()
      const matchName = u.realName.toLowerCase().includes(kw)
      const matchAcc = u.username.toLowerCase().includes(kw)
      const matchWork = u.workNo.toLowerCase().includes(kw)
      const matchPhone = (u.phone || '').includes(kw)
      if (!matchName && !matchAcc && !matchWork && !matchPhone) return false
    }
    return true
  })
})

const userDialogVisible = ref(false)
const isEditingUser = ref(false)
const userForm = ref({
  id: null,
  username: '',
  realName: '',
  workNo: '',
  phone: '',
  email: '',
  orgName: '中共红河数据产业集团有限公司总支部委员会',
  roleCode: 'PARTY_MEMBER',
  roleName: '普通在册党员 / 发展成员',
  wecomUserId: '', dingtalkUserId: '', password: ''
})

const roleAuthDialogVisible = ref(false)
const currentAuthorizingUser = ref(null)
const selectedRoleCodeForAssign = ref('')

function openCreateRoleDialog() {
  ElMessage.info('支持在后台配置新增党务角色与权限标识')
}

function handleRoleSwitch(newRole) {
  const roleNameMap = {
    committee_organizer: '党总支组织员 (集团组织科)',
    branch_secretary: '红数信息党支部书记',
    branch_secretary_mc: '幂次科技党支部书记',
    branch_secretary_ld: '链达科技党支部书记',
    discipline_inspector: '党总支纪检委员 (纪检把关)',
    sys_admin: '系统超级管理员 (权限与渠道)',
    member_self: '发展成员本人 (工号HH-HS-012)'
  }
  ElMessage.success(`已切换当前登录身份为：${roleNameMap[newRole] || newRole}`)
}

function openCreateUserDialog() {
  isEditingUser.value = false
  userForm.value = {
    id: null,
    username: '',
    realName: '',
    workNo: '',
    phone: '',
    email: '',
    orgName: '中共红河数据产业集团有限公司总支部委员会',
    roleCode: 'PARTY_MEMBER',
    roleName: '普通在册党员 / 发展成员',
    wecomUserId: '', dingtalkUserId: '', password: ''
  }
  userDialogVisible.value = true
}

function editSysUser(row) {
  isEditingUser.value = true
  userForm.value = {
    id: row.id,
    username: row.username,
    realName: row.realName,
    workNo: row.workNo,
    phone: row.phone,
    email: row.email,
    orgName: row.orgName,
    roleCode: row.roleCode,
    roleName: row.roleName,
    wecomUserId: row.wecomUserId || '', dingtalkUserId: row.dingtalkUserId || '', password: ''
  }
  userDialogVisible.value = true
}

function handleUserFormRoleChange(roleCode) {
  const r = sysRoles.value.find(item => item.roleCode === roleCode)
  if (r) {
    userForm.value.roleName = r.roleName
  }
}

async function saveSysUser() {
  if (!userForm.value.username.trim() || !userForm.value.realName.trim() || !userForm.value.workNo.trim()) {
    ElMessage.warning('请填写必填项：账号、姓名、工号！')
    return
  }

  if (apiSession.value) {
    const role = sysRoles.value.find(item => item.roleCode === userForm.value.roleCode)
    if (!role) return ElMessage.warning('请选择有效角色')
    try {
      const body = { username: userForm.value.username.trim(), realName: userForm.value.realName.trim(), workNo: userForm.value.workNo.trim(),
        phone: userForm.value.phone, email: userForm.value.email, orgName: userForm.value.orgName,
        orgId: getCompanyByBranch(userForm.value.orgName)?.orgId, wecomUserId: userForm.value.wecomUserId, dingtalkUserId: userForm.value.dingtalkUserId }
      if (userForm.value.password) body.password = userForm.value.password
      const saved = await apiRequest(isEditingUser.value ? `/auth/users/${userForm.value.id}` : '/auth/users', {
        method: isEditingUser.value ? 'PUT' : 'POST', body
      })
      userForm.value.id = saved.id
      isEditingUser.value = true
      userForm.value.password = ''
      await apiRequest(`/auth/users/${saved.id}/roles`, { method: 'POST', body: [role.id] })
      userDialogVisible.value = false
      await loadSysUsers()
      ElMessage.success('账号、接收地址和角色已保存到服务器')
    } catch (error) { ElMessage.error(error.message) }
    return
  }

  if (isEditingUser.value) {
    const idx = sysUsers.value.findIndex(u => u.id === userForm.value.id)
    if (idx !== -1) {
      Object.assign(sysUsers.value[idx], {
        realName: userForm.value.realName,
        workNo: userForm.value.workNo,
        phone: userForm.value.phone,
        email: userForm.value.email,
        orgName: userForm.value.orgName,
        roleCode: userForm.value.roleCode,
        roleName: userForm.value.roleName
      })
    }
    userDialogVisible.value = false
    ElMessage.success('用户账号资料已成功修改！')
  } else {
    sysUsers.value.unshift({
      id: Date.now(),
      username: userForm.value.username.trim().toLowerCase(),
      realName: userForm.value.realName.trim(),
      workNo: userForm.value.workNo.trim(),
      phone: userForm.value.phone,
      email: userForm.value.email,
      orgId: 1,
      orgName: userForm.value.orgName,
      roleCode: userForm.value.roleCode,
      roleName: userForm.value.roleName,
      status: 1,
      lastLoginTime: '未登录',
      createdAt: new Date().toISOString().slice(0, 10)
    })
    userDialogVisible.value = false
    ElMessage.success('新党务用户账号创建成功！初始默认密码为 123456。')
  }
}

function assignUserRoles(row) {
  currentAuthorizingUser.value = row
  selectedRoleCodeForAssign.value = row.roleCode
  roleAuthDialogVisible.value = true
}

async function confirmAssignRole() {
  if (!currentAuthorizingUser.value || !selectedRoleCodeForAssign.value) return
  const r = sysRoles.value.find(item => item.roleCode === selectedRoleCodeForAssign.value)
  if (apiSession.value && r) {
    try {
      await apiRequest(`/auth/users/${currentAuthorizingUser.value.id}/roles`, { method: 'POST', body: [r.id] })
      roleAuthDialogVisible.value = false
      await loadSysUsers()
      ElMessage.success('角色授权已保存到服务器')
    } catch (error) { ElMessage.error(error.message) }
    return
  }
  if (r) {
    currentAuthorizingUser.value.roleCode = r.roleCode
    currentAuthorizingUser.value.roleName = r.roleName
  }
  roleAuthDialogVisible.value = false
  ElMessage.success(`已为【${currentAuthorizingUser.value.realName}】指派角色为：${r.roleName}`)
}

async function toggleUserStatus(row) {
  if (apiSession.value) {
    try {
      await apiRequest(`/auth/users/${row.id}`, { method: 'PUT', body: { status: row.status === 1 ? 0 : 1 } })
      await loadSysUsers()
      ElMessage.success('账号状态已保存到服务器')
    } catch (error) { ElMessage.error(error.message) }
    return
  }
  row.status = row.status === 1 ? 0 : 1
  ElMessage.success(`账号【${row.realName}】已切换为：${row.status === 1 ? '启用正常' : '已停用禁用'}`)
}

// ==========================================
// 4. 一人一档抽屉与合规防错引擎交互
// ==========================================
const drawerVisible = ref(false)
const currentMember = ref(null)
const selectedStepInDrawer = ref(1)
const previewDialogVisible = ref(false)
const previewDocTitle = ref('材料详情')

const all25StepsFlat = computed(() => {
  const list = []
  STAGES_AND_STEPS.forEach(st => {
    st.steps.forEach(sp => {
      list.push(sp)
    })
  })
  return list
})

const activeDrawerStepInfo = computed(() => {
  return all25StepsFlat.value.find(s => s.stepId === selectedStepInDrawer.value) || all25StepsFlat.value[0]
})

const stepMaterialsList = computed(() => {
  if (!currentMember.value) return []
  return activeDrawerStepInfo.value.docs.map((docName, idx) => {
    return {
      id: idx + 1,
      name: docName,
      status: selectedStepInDrawer.value <= currentMember.value.currentStepId ? 'approved' : 'pending',
      time: selectedStepInDrawer.value < currentMember.value.currentStepId ? '已归档' : (selectedStepInDrawer.value === currentMember.value.currentStepId ? '2025-03-18' : '待流转办理')
    }
  })
})

const activeDrawerStepAuditClass = computed(() => {
  if (!currentMember.value) return 'audit-normal'
  if (currentMember.value.id === 101 && selectedStepInDrawer.value >= 9) return 'audit-danger'
  if (currentMember.value.id === 105 && selectedStepInDrawer.value === 2) return 'audit-warning'
  return 'audit-pass'
})

const activeDrawerStepAuditDesc = computed(() => {
  if (!currentMember.value) return ''
  if (currentMember.value.id === 101 && selectedStepInDrawer.value >= 9) {
    return '【系统强阻断】检测到该成员确定为入党积极分子仅 290 天（未满 365 天）。依据《细则》第十三条，考察期不足一年不得列为发展对象，系统阻断推进！'
  }
  if (currentMember.value.id === 105 && selectedStepInDrawer.value === 2) {
    return '【临期预警】入党申请书递交已过 22 天，距离“1个月内必须完成支部谈话”红线仅剩 8 天，请尽快组织谈话并归档《谈话记录表》。'
  }
  return '【合规审计通过】该步骤前置环节时间顺序合法、材料齐备，符合《中国共产党发展党员工作细则》要求。'
})

function getStepName(stepId) {
  const step = all25StepsFlat.value.find(s => s.stepId === stepId)
  return step ? step.name : ''
}

function getStageTagType(stageId) {
  const map = { 1: 'info', 2: 'warning', 3: 'primary', 4: 'danger', 5: 'success' }
  return map[stageId] || 'info'
}

function getComplianceShortText(alert) {
  if (!alert) return '合规正常'
  if (alert.type === 'danger') return '考察不足1年(锁死)'
  if (alert.type === 'warning') return '临期预警待办'
  return '合规流转中'
}

function getMaterialTagType(status) {
  const map = { approved: 'success', pending: 'warning', waiting_upload: 'danger' }
  return map[status] || 'info'
}

function getMaterialStatusText(status) {
  const map = { approved: '已审核通过', pending: '待组织审核', waiting_upload: '待上传提交' }
  return map[status] || '未开始'
}

function openMemberDrawer(member) {
  currentMember.value = member
  selectedStepInDrawer.value = member.currentStepId
  drawerVisible.value = true
}

function quickProgressStep(member) {
  openMemberDrawer(member)
}

function handleAdvanceStep() {
  // 1. 角色推进权限拦截：只有总支管理员和支部管理员有权推进业务流转
  if (!hasPermission('workbench:advance') && !hasPermission('workbench:audit')) {
    ElMessageBox.alert('您当前所属角色为只读模式，无权审批或推进党员发展规程！', '权限不足', { type: 'warning' })
    return
  }

  // 2. 党总支专属审批步骤越权防护（第20步接收审批、第25步转正审批仅限党总支管理员/超管）
  if ((selectedStepInDrawer.value === 20 || selectedStepInDrawer.value === 25) && isBranchAdmin.value) {
    ElMessageBox.alert(
      `第 ${selectedStepInDrawer.value} 步依据《中国共产党发展党员工作细则》必须由【中共红河数据产业集团有限公司总支部委员会】集体研究审批，子公司党支部无权直接批复，请等待党总支审批下达！`,
      '党总支审批权限拦截',
      { type: 'warning' }
    )
    return
  }

  // 3. 积极分子未满 365 天系统合规硬阻断
  if (currentMember.value.id === 101 && currentMember.value.currentStepId === 7) {
    ElMessageBox.alert(
      '【系统硬阻断】张强同志作为入党积极分子考察期仅 290 天（未满法定 365 天）。依据《中国共产党发展党员工作细则》第十三条，严禁在考察期不足一年时提前确定为发展对象！如遇巡视巡察将判定为违规入党。',
      '党务合规审计阻断',
      { confirmButtonText: '已知晓并严格按规章执行', type: 'error' }
    )
    return
  }

  const today = new Date().toISOString().slice(0, 10)
  const currentStep = selectedStepInDrawer.value
  const mem = membersList.value.find(m => m.id === currentMember.value.id)

  // 4. 关键节点一：若为第 25 步（预备党员转正审批与归档）
  if (currentStep === 25) {
    if (mem) {
      mem.currentStageId = 5
      mem.currentStepId = 25
      mem.stepStatus = 'finished'
      mem.officialDate = today
    }
    // 据实同步花名册为【正式党员】（1），更新正式转正日期，开启党龄折算
    syncMemberToRoster(currentMember.value, 1, {
      partyPost: '普通党员',
      officialPartyDate: today
    })
    ElMessageBox.alert(
      `热烈祝贺！【${currentMember.value.name}】同志预备党员转正申请已获党总支审批通过，正式成为中国共产党正式党员！系统已据实将花名册政治面貌同步升级为【正式党员】，并开始折算党龄。`,
      '转正审批通过 · 花名册据实同步为正式党员',
      { type: 'success', confirmButtonText: '查看花名册档案' }
    ).then(() => {
      activeTab.value = 'roster'
    }).catch(() => {})
    return
  }

  // 5. 关键节点二：若为第 20 步（基层党委审批预备党员）
  if (currentStep === 20) {
    if (mem) {
      mem.currentStepId = 21 // 推进至第21步（入党宣誓与预备期考察）
      mem.currentStageId = 5
      mem.probationaryDate = today
      selectedStepInDrawer.value = 21
    }
    // 据实同步花名册为【预备党员】（2），记录入党时间
    syncMemberToRoster(currentMember.value, 2, {
      partyPost: '预备党员',
      joinPartyDate: today
    })
    ElMessageBox.alert(
      `【${currentMember.value.name}】同志接收为预备党员申请已获党总支审批通过，正式成为中国共产党预备党员！系统已据实将花名册政治面貌同步升级为【预备党员】（入党时间：${today}），请及时组织入党宣誓。`,
      '审批通过 · 花名册据实同步为预备党员',
      { type: 'success', confirmButtonText: '查看花名册档案' }
    ).then(() => {
      activeTab.value = 'roster'
    }).catch(() => {})
    return
  }

  // 6. 普通步骤推进与流转判定
  let nextStep = currentStep
  if (mem && currentStep === mem.currentStepId && mem.currentStepId < 25) {
    nextStep = mem.currentStepId + 1
    mem.currentStepId = nextStep
    if (nextStep >= 21) mem.currentStageId = 5
    else if (nextStep >= 17) mem.currentStageId = 4
    else if (nextStep >= 12) mem.currentStageId = 3
    else if (nextStep >= 6) mem.currentStageId = 2
    else mem.currentStageId = 1
    selectedStepInDrawer.value = nextStep
  }

  // 7. 关键节点三：步骤跨入第 12 步（确定发展对象）
  if (nextStep >= 12 && nextStep < 17) {
    if (mem && !mem.targetDate) mem.targetDate = today
    syncMemberToRoster(currentMember.value, 3, { partyPost: '发展对象' })
    ElMessage.success(`第 ${currentStep} 步审核归档通过！已成功推进至第 ${nextStep} 步。该同志已确定为【发展对象】，花名册政治面貌已据实同步升级为【发展对象】！`)
    return
  }

  // 8. 关键节点四：步骤跨入第 6 步（确定入党积极分子）
  if (nextStep >= 6 && nextStep < 12) {
    if (mem && !mem.activistDate) mem.activistDate = today
    syncMemberToRoster(currentMember.value, 4, { partyPost: '积极分子' })
    ElMessage.success(`第 ${currentStep} 步审核归档通过！已成功推进至第 ${nextStep} 步。该同志已确定为【入党积极分子】，花名册政治面貌已据实同步升级为【入党积极分子】！`)
    return
  }

  // 9. 阶段一内部常规流转（第 1~5 步）：保持入党申请人状态
  if (nextStep < 6) {
    syncMemberToRoster(currentMember.value, 5, { partyPost: '入党申请人' })
  }

  ElMessage.success({ message: `第 ${currentStep} 步审核归档通过！已成功推进至第 ${nextStep} 步业务节点。`, duration: 3000 })
}

function previewMaterial(row) {
  previewDocTitle.value = row.name
  previewDialogVisible.value = true
}

function downloadSingleDoc(row) {
  ElMessage.success(`已生成 ${row.name}（已自动填充 ${currentMember.value.name} 组织信息与编号），已下载！`)
}

function mockBatchExportDocs() {
  ElMessage.success(`已一键打包生成【${currentMember.value.name}】全流程档案（包含 20+ 份规范 Word/PDF），ZIP 压缩包已导出！`)
}

function downloadDocSuccess() {
  ElMessage.success(`已成功导出 ${previewDocTitle.value} Word 格式文档！`)
  previewDialogVisible.value = false
}

function exportTableData() {
  ElMessage.success('已导出红河数据产业集团 2025 年度发展党员合规台账（Excel格式）。')
}
</script>

<style scoped>
.dj-root {
  min-height: 100vh;
}

.cross-unit-alert {
  margin: 4px 0 16px 0;
  padding: 10px 14px;
  background-color: #fdf6ec;
  border: 1px solid #faecd8;
  border-radius: 6px;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  font-size: 13px;
  line-height: 1.5;
  color: #b88230;
}
.cross-unit-alert .el-icon {
  font-size: 16px;
  margin-top: 2px;
  color: #e6a23c;
  flex-shrink: 0;
}

/* ========================================================================= */
/* 普通业务端样式                                                            */
/* ========================================================================= */
.dj-app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.dj-header {
  height: 68px;
  background: linear-gradient(90deg, #990f10 0%, #c21c1d 60%, #a61214 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  box-shadow: 0 2px 10px rgba(153, 15, 16, 0.25);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.logo-badge {
  width: 42px;
  height: 42px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.title-group h1 {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.5px;
  margin: 0;
  color: #fff;
}

.sub-title {
  font-size: 12px;
  opacity: 0.88;
  color: #ffeaea;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.big-screen-btn {
  font-weight: bold;
  border: 1px solid #ffbe4f !important;
}

.role-switcher {
  display: flex;
  align-items: center;
  gap: 6px;
}

.role-label {
  font-size: 13px;
  color: #ffeaea;
}

.org-tag {
  background: rgba(0, 0, 0, 0.2) !important;
  border: 1px solid rgba(255, 255, 255, 0.3) !important;
}

/* 跑马灯合规警报条 */
.compliance-marquee {
  background: #fff8e6;
  border-bottom: 1px solid #f9e2ae;
  padding: 8px 28px;
  display: flex;
  align-items: center;
  gap: 20px;
  overflow: hidden;
  height: 44px;
  box-sizing: border-box;
}

.marquee-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #c21c1d;
  font-weight: bold;
  font-size: 13px;
  white-space: nowrap;
  flex-shrink: 0;
  z-index: 2;
  background: #fff8e6;
  padding-right: 12px;
  box-shadow: 4px 0 8px #fff8e6;
}

.marquee-tag .el-icon {
  font-size: 14px;
  color: #c21c1d;
}

.marquee-track-container {
  flex: 1;
  overflow: hidden;
  position: relative;
  display: flex;
}

.marquee-scroller {
  display: flex;
  align-items: center;
  gap: 40px;
  white-space: nowrap;
  animation: marquee-roll-left 32s linear infinite;
  will-change: transform;
}

/* 鼠标悬停（hover）暂停平滑滚动 */
.marquee-track-container:hover .marquee-scroller {
  animation-play-state: paused;
}

@keyframes marquee-roll-left {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(-50%);
  }
}

.alert-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 12.5px;
  cursor: pointer;
  padding: 3px 12px;
  border-radius: 16px;
  transition: all 0.2s ease;
  user-select: none;
}

.alert-item:hover {
  background: rgba(194, 28, 29, 0.08);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.alert-item .alert-text {
  transition: color 0.2s;
}

.alert-item:hover .alert-text {
  color: #c21c1d;
  text-decoration: underline;
}

.alert-item .click-hint {
  font-size: 11px;
  color: #909399;
  opacity: 0;
  transform: translateX(-4px);
  transition: all 0.2s ease;
}

.alert-item:hover .click-hint {
  opacity: 1;
  transform: translateX(0);
  color: #c21c1d;
  font-weight: bold;
}

.alert-danger .alert-text {
  color: #c21c1d;
  font-weight: 500;
}

.alert-warning .alert-text {
  color: #b88230;
}

.alert-info .alert-text {
  color: #409eff;
}

.dj-main-container {
  padding: 20px 28px 40px;
  flex: 1;
}

/* ========================================================================= */
/* 独立现代白底主导航卡片栏：彻底解耦顶部栏，拥有通透留白与呼吸感                */
/* ========================================================================= */
.dj-nav-deck {
  background: #ffffff;
  border-radius: 12px;
  padding: 10px 16px;
  margin-bottom: 22px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04), 0 1px 3px rgba(0, 0, 0, 0.02);
  border: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px 14px; /* 舒适宽松的行列间隙，彻底告别左右拥挤 */
}

/* 单个胶囊药丸导航项：左右舒展、呼吸感极强 */
.deck-nav-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 9px 18px;
  border-radius: 8px;
  font-size: 14.5px;
  font-weight: 500;
  color: #475569;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  user-select: none;
  white-space: nowrap;
}

/* 悬停态：优雅浅红微浮动 */
.deck-nav-item:hover {
  color: #c21c1d;
  background-color: #fff5f5;
  border-color: #fecaca;
  transform: translateY(-1px);
}

.deck-nav-item .deck-item-icon {
  font-size: 16px;
  opacity: 0.85;
  transition: transform 0.25s ease;
}

.deck-nav-item:hover .deck-item-icon {
  transform: scale(1.15);
  opacity: 1;
}

/* 选中激活态：高质感党建红渐变卡片 + 纯白高亮 + 柔和党建红微光投影 */
.deck-nav-item.is-active {
  color: #ffffff;
  background: linear-gradient(135deg, #c21c1d 0%, #990f10 100%);
  border-color: #990f10;
  font-weight: 600;
  box-shadow: 0 4px 14px rgba(194, 28, 29, 0.28);
  transform: translateY(-1px);
}

.deck-nav-item.is-active .deck-item-icon {
  color: #ffffff;
  opacity: 1;
}

.deck-item-badge {
  margin-left: 2px;
}
.deck-item-badge :deep(.el-badge__content) {
  background-color: #f56c6c;
  border: 1.5px solid #fff;
  font-weight: bold;
}
.deck-nav-item.is-active .deck-item-badge :deep(.el-badge__content) {
  background-color: #ffd04b;
  color: #8b0000;
  border-color: #c21c1d;
}

/* 隐藏主体内容区 el-tabs 头部，由独立白底主导航卡片栏直接驱动视图切换 */
.dj-content-tabs :deep(.el-tabs__header) {
  display: none !important;
}

.dj-content-tabs :deep(.el-tabs__content) {
  overflow: visible !important;
}

/* 5大阶段横幅 */
.stages-overview-card {
  background: #fff;
  border-radius: 8px;
  padding: 18px 22px;
  margin-bottom: 18px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.card-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.card-header-row h3 {
  font-size: 15px;
  font-weight: 700;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 6px;
}

.rule-hint {
  font-size: 12px;
  color: #909399;
}

.stages-stepper {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}

.stage-step-item {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 12px 14px;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.25s ease;
  position: relative;
}

.stage-step-item:hover {
  border-color: #c21c1d;
  background: #fff5f5;
  transform: translateY(-2px);
}

.stage-step-item.active-stage {
  border-color: #c21c1d;
  background: #fef0f0;
  box-shadow: 0 0 0 1px #c21c1d inset;
}

.stage-index-num {
  font-size: 20px;
  font-weight: 800;
  color: #c21c1d;
  opacity: 0.85;
}

.stage-info {
  flex: 1;
}

.stage-title {
  font-size: 13px;
  font-weight: bold;
  color: #2d3748;
  margin-bottom: 4px;
}

.stage-meta {
  font-size: 11.5px;
  color: #718096;
  display: flex;
  align-items: center;
  gap: 8px;
}

.stage-arrow {
  color: #cbd5e1;
}

/* 列表卡片与工具栏 */
.members-table-card, .roster-container-card, .templates-mgr-container {
  background: #fff;
  border-radius: 8px;
  padding: 18px 22px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 花名册统计横幅 */
.roster-stats-banner {
  background: #fdf6ec;
  border: 1px solid #faecd8;
  border-radius: 8px;
  padding: 14px 20px;
  display: flex;
  align-items: center;
  justify-content: space-around;
  margin-bottom: 18px;
}

.stat-pill {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.pill-label {
  font-size: 12px;
  color: #606266;
}

.pill-val {
  font-size: 20px;
  font-weight: 800;
  color: #303133;
}

.color-red { color: #c21c1d !important; }
.color-orange { color: #e6a23c !important; }
.color-blue { color: #409eff !important; }
.color-green { color: #67c23a !important; }
.color-gold { color: #f4d03f !important; }
.color-cyan { color: #00e5ff !important; }

.roster-name-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sub-workno {
  font-size: 11px;
  color: #909399;
}

.standing-text {
  color: #c21c1d;
  font-size: 13.5px;
}

.date-col {
  display: flex;
  flex-direction: column;
  font-size: 11.5px;
}

.code-font {
  font-family: monospace;
  font-size: 11.5px;
  color: #606266;
}

/* 模板管理页 */
.mgr-header-card {
  background: #fdf2f2;
  border: 1px solid #fcdada;
  border-radius: 8px;
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.mgr-header-left h3 {
  margin: 0 0 6px 0;
  font-size: 16px;
  color: #c21c1d;
  display: flex;
  align-items: center;
  gap: 6px;
}

.mgr-header-left p {
  margin: 0;
  font-size: 12.5px;
  color: #606266;
}

.mgr-header-right {
  display: flex;
  gap: 10px;
}

.tpl-name-cell {
  display: flex;
  flex-direction: column;
}

.tpl-code-text {
  font-size: 11px;
  color: #909399;
}

.tpl-status-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}

.version-tag {
  font-size: 11px;
  color: #909399;
}

.tpl-file-meta {
  display: flex;
  flex-direction: column;
  font-size: 12px;
}

.custom-file-name {
  color: #b88230;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
}

.default-file-name {
  color: #606266;
  display: flex;
  align-items: center;
  gap: 4px;
}

.update-sub {
  font-size: 11px;
  color: #a0aec0;
}

/* 驾驶舱与工作台通用卡片 */
.member-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-red {
  background-color: #c21c1d !important;
  color: #fff;
  font-weight: bold;
}

.avatar-red-big {
  background-color: #c21c1d !important;
  color: #fff;
  font-size: 20px;
  font-weight: bold;
}

.member-meta {
  display: flex;
  flex-direction: column;
}

.name-line {
  display: flex;
  align-items: center;
  gap: 6px;
}

.sub-job {
  font-size: 12px;
  color: #909399;
}

.branch-cell {
  display: flex;
  flex-direction: column;
}

.branch-title {
  font-weight: 600;
  font-size: 13px;
  color: #303133;
}

.dept-title {
  font-size: 11.5px;
  color: #909399;
}

.tags-wrapper {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.step-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.step-name {
  font-size: 12.5px;
  color: #2c3e50;
}

.stay-days {
  font-size: 11px;
  color: #909399;
}

.compliance-cell {
  display: flex;
  align-items: center;
}

.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

/* 驾驶舱样式 */
.cockpit-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.metric-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.metric-card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.metric-title {
  font-size: 13.5px;
  font-weight: 600;
  color: #4a5568;
}

.metric-numbers {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 10px;
}

.actual-val {
  font-size: 26px;
  font-weight: 800;
  color: #c21c1d;
}

.target-val {
  font-size: 11.5px;
  color: #718096;
}

.metric-desc {
  font-size: 11.5px;
  color: #718096;
  margin-top: 8px;
}

.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.chart-box {
  background: #fff;
  border-radius: 8px;
  padding: 18px 22px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f2f5;
}

.chart-header h4 {
  margin: 0;
  font-size: 14.5px;
  display: flex;
  align-items: center;
  gap: 6px;
  color: #303133;
}

.plan-stat {
  font-size: 12px;
  color: #c21c1d;
  font-weight: 600;
}

.funnel-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px 0;
}

.funnel-stage {
  height: 42px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
}

.stage-1 { background: #909399; width: 100%; }
.stage-2 { background: #e6a23c; width: 85%; margin: 0 auto; }
.stage-3 { background: #409eff; width: 70%; margin: 0 auto; }
.stage-4 { background: #e74c3c; width: 55%; margin: 0 auto; }
.stage-5 { background: #c21c1d; width: 42%; margin: 0 auto; }

.branch-progress-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.branch-item .b-info {
  display: flex;
  justify-content: space-between;
  font-size: 12.5px;
  color: #4a5568;
  margin-bottom: 6px;
}

.dual-cultivate-card {
  background: #fff;
  border-radius: 8px;
  padding: 18px 22px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.dual-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.dual-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
}

.dual-stat-box {
  background: #fdf6ec;
  border: 1px solid #faecd8;
  border-radius: 6px;
  padding: 16px;
  text-align: center;
}

.dual-stat-box .num-text {
  font-size: 24px;
  font-weight: 800;
  color: #b88230;
  margin-bottom: 4px;
}

.dual-stat-box .sub-text {
  font-size: 12px;
  color: #606266;
}

/* 抽屉样式 */
.drawer-inner-layout {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-bottom: 20px;
}

.member-header-card {
  background: #fdf2f2;
  border: 1px solid #fcdada;
  border-radius: 8px;
  padding: 18px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profile-left {
  display: flex;
  align-items: center;
  gap: 18px;
}

.main-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.main-info h2 {
  margin: 0;
  font-size: 20px;
  color: #2c3e50;
}

.dept-info {
  display: flex;
  gap: 16px;
  font-size: 12.5px;
  color: #606266;
}

.dept-info span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.profile-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.cultivator-box {
  font-size: 12.5px;
  color: #606266;
}

.drawer-compliance-alert {
  margin: 0;
}

.drawer-split-body {
  display: grid;
  grid-template-columns: 290px 1fr;
  gap: 20px;
  min-height: 520px;
}

.timeline-sidebar {
  border-right: 1px solid #e4e7ed;
  padding-right: 14px;
  max-height: 600px;
  overflow-y: auto;
}

.sidebar-header {
  margin-bottom: 12px;
}

.sidebar-header h4 {
  font-size: 14px;
  margin: 0 0 2px 0;
  color: #303133;
}

.sidebar-header .hint {
  font-size: 11px;
  color: #909399;
}

.steps-timeline-list {
  display: flex;
  flex-direction: column;
}

.timeline-node {
  display: flex;
  gap: 10px;
  cursor: pointer;
  padding: 6px 8px;
  border-radius: 6px;
  transition: background 0.2s;
}

.timeline-node:hover {
  background: #f5f7fa;
}

.timeline-node.node-selected {
  background: #fef0f0;
}

.node-icon-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 24px;
}

.icon-circle {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: bold;
}

.icon-circle.done {
  background: #67c23a;
  color: #fff;
}

.icon-circle.current {
  background: #c21c1d;
  color: #fff;
  box-shadow: 0 0 0 3px rgba(194, 28, 29, 0.25);
  animation: pulse 1.8s infinite;
}

.icon-circle.future {
  background: #e4e7ed;
  color: #909399;
}

.line-segment {
  width: 2px;
  flex: 1;
  background: #e4e7ed;
  min-height: 24px;
  margin-top: 4px;
}

.node-info-col {
  flex: 1;
}

.node-title {
  font-size: 12.5px;
  font-weight: 600;
  color: #2c3e50;
}

.node-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 11px;
  margin-top: 2px;
}

.n-role {
  color: #909399;
}

.pulse-tag {
  color: #c21c1d;
  font-weight: bold;
}

.done-tag {
  color: #67c23a;
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(194, 28, 29, 0.5); }
  70% { box-shadow: 0 0 0 6px rgba(194, 28, 29, 0); }
  100% { box-shadow: 0 0 0 0 rgba(194, 28, 29, 0); }
}

.step-detail-main {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.step-main-header {
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 12px;
}

.header-tit {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.header-tit h3 {
  margin: 0;
  font-size: 17px;
  color: #2c3e50;
}

.rule-box {
  font-size: 12.5px;
  color: #606266;
  background: #f8fafc;
  padding: 8px 12px;
  border-radius: 4px;
}

.audit-radar-box {
  border-radius: 6px;
  padding: 12px 16px;
}

.audit-danger {
  background: #fef0f0;
  border: 1px solid #fde2e2;
  color: #c21c1d;
}

.audit-warning {
  background: #fdf6ec;
  border: 1px solid #faecd8;
  color: #b88230;
}

.audit-pass {
  background: #f0f9eb;
  border: 1px solid #e1f3d8;
  color: #67c23a;
}

.radar-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: bold;
  font-size: 13.5px;
  margin-bottom: 4px;
}

.radar-p {
  margin: 0;
  font-size: 12.5px;
}

.materials-section {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 14px;
}

.sec-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.sec-header h4 {
  margin: 0;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.step-footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 14px;
  border-top: 1px solid #ebeef5;
}

/* 预览弹窗纸质感 */
.doc-preview-modal-body {
  background: #eaeaea;
  padding: 20px;
  display: flex;
  justify-content: center;
}

.doc-paper {
  background: #fff;
  width: 100%;
  max-width: 520px;
  min-height: 380px;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.15);
  padding: 30px;
  font-family: "SimSun", "Songti SC", serif;
}

.doc-paper-header {
  text-align: center;
  border-bottom: 2px solid #c21c1d;
  padding-bottom: 12px;
  margin-bottom: 18px;
}

.doc-paper-header h3 {
  font-size: 18px;
  color: #c21c1d;
  letter-spacing: 1px;
  margin: 0 0 6px 0;
}

.doc-code {
  font-size: 11px;
  color: #909399;
}

.doc-paper-content p {
  font-size: 13.5px;
  line-height: 1.8;
  margin-bottom: 8px;
  color: #333;
}

.sign-seal-box {
  margin-top: 40px;
  text-align: right;
  font-size: 13px;
  position: relative;
}

.seal-mark {
  color: rgba(194, 28, 29, 0.5);
  font-weight: bold;
  font-size: 15px;
  margin-bottom: 4px;
}

.sign-line {
  margin-bottom: 4px;
}

.date-line {
  color: #666;
}

/* ========================================================= */
/* 通知中心与渠道配置样式                                      */
/* ========================================================= */
.notices-view-container, .users-view-container {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.channel-status-cards, .roles-summary-card {
  background: #fff;
  border-radius: 8px;
  padding: 18px 22px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.channel-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.title-with-desc h3 {
  font-size: 15px;
  font-weight: 700;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 4px 0;
}

.sub-tip {
  font-size: 12px;
  color: #909399;
}

.channel-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 14px;
}

.channel-box {
  background: #fdfaf5;
  border: 1px solid #faecd8;
  border-radius: 8px;
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  transition: all 0.2s;
}

.channel-box:hover {
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.08);
}

.channel-disabled {
  opacity: 0.65;
  background: #f5f7fa;
  border-color: #e4e7ed;
}

.channel-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.channel-identity {
  display: flex;
  align-items: center;
  gap: 6px;
}

.ch-icon {
  color: #c21c1d;
}

.ch-name {
  font-weight: 700;
  font-size: 13.5px;
  color: #303133;
}

.channel-desc {
  font-size: 12px;
  color: #606266;
  line-height: 1.5;
  margin-bottom: 12px;
  min-height: 36px;
}

.channel-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-top: 1px dashed #ebeef5;
  padding-top: 8px;
}

.notices-table-card, .users-table-card {
  background: #fff;
  border-radius: 8px;
  padding: 18px 22px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.unread-tag {
  color: #c21c1d;
  font-size: 12px;
  font-weight: bold;
}

.read-tag {
  color: #909399;
  font-size: 12px;
}

.unread-title {
  font-weight: bold;
  color: #1a1a1a;
}

.notice-detail-view {
  padding: 6px 4px;
}

.nd-title {
  font-size: 16px;
  color: #1a1a1a;
  margin: 0 0 10px 0;
  line-height: 1.4;
}

.nd-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: #909399;
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.nd-content {
  font-size: 14px;
  line-height: 1.8;
  color: #303133;
  background: #fcfcfc;
  padding: 14px 16px;
  border-radius: 6px;
  border: 1px solid #f2f2f2;
  margin-bottom: 16px;
}

.nd-footer {
  font-size: 12.5px;
  color: #606266;
  line-height: 1.6;
}

/* ========================================================= */
/* RBAC 角色与用户样式                                         */
/* ========================================================= */
.roles-cards-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}

.role-stat-box {
  background: #fbfbfc;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
}

.role-box-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.role-badge-title {
  font-weight: 700;
  font-size: 13px;
  color: #303133;
}

.role-box-desc {
  font-size: 11.5px;
  color: #606266;
  line-height: 1.5;
  min-height: 36px;
  margin: 0 0 8px 0;
}

.role-perms-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.perm-tag {
  font-size: 10.5px;
  padding: 0 4px;
  height: 20px;
  line-height: 20px;
}

.user-name-cell {
  display: flex;
  flex-direction: column;
}

.user-acc {
  font-size: 11px;
  color: #909399;
}

.header-badge-item {
  margin-right: 6px;
}

.user-profile-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(0, 0, 0, 0.04);
  padding: 3px 8px 3px 4px;
  border-radius: 16px;
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.user-avatar-small {
  background: #c21c1d;
  color: #fff;
  font-size: 12px;
  font-weight: bold;
}

.user-realname {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.current-auth-role-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.12);
  padding: 4px 10px;
  border-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.22);
}

.role-static-label {
  font-size: 11.5px;
  color: #ffe8e8;
  white-space: nowrap;
}

.role-badge-static {
  font-weight: 600;
  letter-spacing: 0.3px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.user-name-dept-box {
  display: flex;
  align-items: center;
  gap: 4px;
}

.user-workno {
  font-size: 11px;
  color: #8c939d;
}

/* ========================================================================= */
/* 党员转接及调整备案模块样式                                                */
/* ========================================================================= */
.transfer-filing-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sub-tab-nav-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #ffffff;
  padding: 12px 18px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.sub-nav-tips {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #64748b;
}

.avatar-green {
  background: #10b981 !important;
  color: #ffffff;
  font-weight: bold;
}

.avatar-gray {
  background: #94a3b8 !important;
  color: #ffffff;
  font-weight: bold;
}

.avatar-red {
  background: #dc2626 !important;
  color: #ffffff;
  font-weight: bold;
}

.doc-code-badge {
  display: inline-block;
  padding: 2px 8px;
  background: #f1f5f9;
  border: 1px solid #cbd5e1;
  border-radius: 4px;
  font-family: monospace;
  font-size: 12px;
  color: #0f172a;
  font-weight: 600;
}

/* 组织关系介绍信与任免凭证样卡 */
.transfer-cert-box {
  background: #fffdfa;
  border: 2px solid #e2d9cc;
  border-radius: 8px;
  padding: 24px;
  position: relative;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}

.cert-header {
  text-align: center;
  position: relative;
  margin-bottom: 16px;
}

.cert-badge {
  display: inline-block;
  padding: 3px 12px;
  background: #dc2626;
  color: #ffffff;
  font-size: 11px;
  font-weight: 700;
  border-radius: 20px;
  letter-spacing: 1px;
  margin-bottom: 8px;
}

.cert-badge.bg-gold {
  background: #d97706;
}

.cert-title {
  font-size: 20px;
  font-weight: 800;
  color: #991b1b;
  margin: 4px 0 8px;
  letter-spacing: 1.5px;
  font-family: "SimSun", "Songti SC", "STSong", serif;
}

.cert-no {
  font-size: 13px;
  color: #78350f;
  font-family: monospace;
  font-weight: 600;
}

.cert-divider {
  height: 2px;
  background: linear-gradient(90deg, transparent, #b91c1c, transparent);
  margin: 12px 0 20px;
}

.cert-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px 18px;
  font-size: 13.5px;
}

.cert-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  line-height: 1.6;
}

.cert-row.full-width {
  grid-column: 1 / -1;
}

.cert-lbl {
  color: #64748b;
  font-weight: 500;
  min-width: 90px;
  flex-shrink: 0;
}

.cert-val {
  color: #1e293b;
}

.cert-val.font-bold {
  font-weight: 700;
}

.cert-val.text-red {
  color: #dc2626;
}

.cert-val.text-blue {
  color: #2563eb;
}

.cert-val.text-muted {
  color: #94a3b8;
}

.cert-val.text-green {
  color: #16a34a;
}

.cert-seal-box {
  margin-top: 32px;
  padding-top: 16px;
  text-align: right;
  position: relative;
  min-height: 90px;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: flex-end;
}

.seal-party-text {
  font-size: 14px;
  font-weight: 700;
  color: #334155;
  margin-bottom: 4px;
}

.seal-date-text {
  font-size: 13px;
  color: #64748b;
  font-family: monospace;
}

.cert-red-stamp {
  position: absolute;
  right: 20px;
  bottom: -6px;
  width: 88px;
  height: 88px;
  border: 3px solid rgba(220, 38, 38, 0.75);
  border-radius: 50%;
  color: rgba(220, 38, 38, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  font-size: 13px;
  font-weight: 800;
  line-height: 1.3;
  transform: rotate(-15deg);
  pointer-events: none;
  box-shadow: 0 0 4px rgba(220, 38, 38, 0.2);
}
</style>
