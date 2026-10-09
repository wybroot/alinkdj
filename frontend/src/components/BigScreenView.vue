<template>
  <div class="solemn-screen-container">
    <!-- ========================================================================= -->
    <!-- 1. 顶部庄重大气抬头 (Symmetrical Dignified SOE Header)                      -->
    <!-- ========================================================================= -->
    <header class="formal-header">
      <div class="header-inner">
        <!-- 左侧信息 -->
        <div class="header-meta-left">
          <div class="org-affiliation">
            <span class="org-dot"></span>
            <strong>中共红河数据产业集团有限公司总支部委员会</strong>
          </div>
          <div class="live-datetime">
            <span>{{ liveDateStr }}</span>
            <span class="time-bold">{{ liveTimeStr }}</span>
          </div>
        </div>

        <!-- 中间大标题 -->
        <div class="header-center-title">
          <div class="title-emblem-row">
            <div class="cpc-flag-badge">
              <PartyEmblem style="width: 32px; height: 32px;" />
            </div>
            <h1>党建工作与发展党员综合调度大屏</h1>
          </div>
          <div class="subtitle-text">
            <span>党总支设5名委员统筹领导</span>
            <span class="sub-sep">·</span>
            <span>直管红数信息、幂次科技、链达科技3家子公司</span>
            <span class="sub-sep">·</span>
            <span>贯彻落实《发展党员工作细则》</span>
          </div>
        </div>

        <!-- 右侧控制区 -->
        <div class="header-meta-right">
          <div class="compliance-badge">
            <span class="shield-icon"><el-icon><CircleCheckFilled /></el-icon></span>
            <span>程序合规率 100%</span>
          </div>
          <button class="exit-btn" @click="$emit('close')">
            <el-icon><Back /></el-icon>
            <span>返回业务系统</span>
          </button>
        </div>
      </div>
      <div class="header-divider"></div>
    </header>

    <!-- ========================================================================= -->
    <!-- 2. 核心 KPI 汇总排 (5 大平衡对称卡片，准确标注党总支与3家子公司)            -->
    <!-- ========================================================================= -->
    <section class="kpi-banner-grid">
      <div class="kpi-box box-total">
        <div class="kpi-content">
          <span class="kpi-label">全集团在册党员总数</span>
          <div class="kpi-val-row">
            <span class="kpi-val gold-num">{{ totalCount }}</span>
            <span class="kpi-unit">人</span>
          </div>
          <span class="kpi-note">在册档案规范率 100%</span>
        </div>
      </div>

      <div class="kpi-box">
        <div class="kpi-content">
          <span class="kpi-label">正式党员 (表决权主体)</span>
          <div class="kpi-val-row">
            <span class="kpi-val white-num">{{ formalCount }}</span>
            <span class="kpi-unit">人</span>
            <span class="kpi-badge">占比 {{ Math.round(formalCount / totalCount * 100) }}%</span>
          </div>
          <span class="kpi-note">平均党龄 11.2 年</span>
        </div>
      </div>

      <div class="kpi-box">
        <div class="kpi-content">
          <span class="kpi-label">预备党员 (期满考察中)</span>
          <div class="kpi-val-row">
            <span class="kpi-val amber-num">{{ probationaryCount }}</span>
            <span class="kpi-unit">人</span>
            <span class="kpi-badge badge-amber">考察满1年</span>
          </div>
          <span class="kpi-note">按季度规范写实考察</span>
        </div>
      </div>

      <div class="kpi-box">
        <div class="kpi-content">
          <span class="kpi-label">重点培养与发展对象</span>
          <div class="kpi-val-row">
            <span class="kpi-val gold-num">{{ developingCount }}</span>
            <span class="kpi-unit">人</span>
            <span class="kpi-badge badge-gold">严格政审把关</span>
          </div>
          <span class="kpi-note">考察不足1年系统锁止</span>
        </div>
      </div>

      <div class="kpi-box">
        <div class="kpi-content">
          <span class="kpi-label">党总支委员 / 直管子公司支部</span>
          <div class="kpi-val-row">
            <span class="kpi-val white-num">5 <small class="text-xs">委员</small> / 3 <small class="text-xs">支部</small></span>
            <span class="kpi-badge badge-green">总支直管</span>
          </div>
          <span class="kpi-note">无党小组 · 扁平化高效贯通</span>
        </div>
      </div>
    </section>

    <!-- ========================================================================= -->
    <!-- 3. 上半部核心三大黄金排版列 (比例 30% : 42% : 28%，恢复此前经典优秀布局)     -->
    <!-- ========================================================================= -->
    <section class="dashboard-body-grid">
      <!-- ----------------------------------------------------------------------- -->
      <!-- 左列 (30%)：队伍画像与党龄结构                                           -->
      <!-- ----------------------------------------------------------------------- -->
      <aside class="grid-col col-left">
        <!-- 卡片 1: 队伍结构与国资考核红线 -->
        <div class="dashboard-card card-team-struct">
          <div class="card-head">
            <div class="head-title">
              <span class="title-bar"></span>
              <h3>党员队伍政治面貌与考核指标</h3>
            </div>
            <span class="head-sub">国资监管达标率 100%</span>
          </div>

          <div class="card-body">
            <div class="donut-chart-box">
              <div ref="statusDonutRef" class="echart-inner"></div>
            </div>

            <div class="quota-check-group">
              <div class="check-item">
                <div class="ci-head">
                  <span class="ci-name">生产经营与业务一线骨干占比</span>
                  <span class="ci-stat"><strong>60.0%</strong> <small class="text-green">达标 (≥40%)</small></span>
                </div>
                <div class="ci-track"><div class="ci-bar" style="width: 60%"></div></div>
              </div>

              <div class="check-item">
                <div class="ci-head">
                  <span class="ci-name">35周岁以下数字青年人才占比</span>
                  <span class="ci-stat"><strong>73.3%</strong> <small class="text-green">达标 (≥50%)</small></span>
                </div>
                <div class="ci-track"><div class="ci-bar" style="width: 73.3%"></div></div>
              </div>

              <div class="check-item">
                <div class="ci-head">
                  <span class="ci-name">技术研发与综合运营骨干占比</span>
                  <span class="ci-stat"><strong>53.3%</strong> <small class="text-green">达标 (≥25%)</small></span>
                </div>
                <div class="ci-track"><div class="ci-bar" style="width: 53.3%"></div></div>
              </div>
            </div>
          </div>
        </div>

        <!-- 卡片 2: 党龄结构分布 -->
        <div class="dashboard-card card-standing">
          <div class="card-head">
            <div class="head-title">
              <span class="title-bar"></span>
              <h3>党员党龄梯队结构分布统计</h3>
            </div>
            <span class="head-sub">梯队结构健全</span>
          </div>
          <div class="card-body">
            <div ref="standingBarRef" class="echart-bar-inner"></div>
          </div>
        </div>
      </aside>

      <!-- ----------------------------------------------------------------------- -->
      <!-- 中列 (42%)：25步流转看板 + 3家子公司推进 + 5名总支委员联系督导矩阵         -->
      <!-- ----------------------------------------------------------------------- -->
      <section class="grid-col col-center">
        <!-- 卡片 3: 发展党员 5 大阶段 25 个步骤全景流转看板 -->
        <div class="dashboard-card card-process-pipeline">
          <div class="card-head">
            <div class="head-title">
              <span class="title-bar"></span>
              <h3>发展党员 5 大阶段、25 项严密规程执行全景</h3>
            </div>
            <span class="head-sub">严格遵循《细则》程序</span>
          </div>

          <div class="card-body">
            <div class="stages-flow-grid">
              <div class="stage-flow-card">
                <div class="s-card-top">
                  <span class="s-idx">01</span>
                  <span class="s-name">申请入党</span>
                </div>
                <div class="s-step-range">第 1~3 步</div>
                <div class="s-rule-desc">书面亲笔申请 · 1个月内谈话 · 建立名册</div>
                <div class="s-stat-row">
                  <span class="s-count">22<small>人</small></span>
                  <span class="s-tag">蓄水池</span>
                </div>
              </div>

              <div class="stage-flow-card">
                <div class="s-card-top">
                  <span class="s-idx">02</span>
                  <span class="s-name">积极分子培养</span>
                </div>
                <div class="s-step-range">第 4~8 步</div>
                <div class="s-rule-desc">群团推优 · 双联系人 · 考察满1年(硬锁) · 季写实</div>
                <div class="s-stat-row">
                  <span class="s-count text-amber">14<small>人</small></span>
                  <span class="s-tag tag-amber">考察中</span>
                </div>
              </div>

              <div class="stage-flow-card">
                <div class="s-card-top">
                  <span class="s-idx">03</span>
                  <span class="s-name">发展对象考察</span>
                </div>
                <div class="s-step-range">第 9~14 步</div>
                <div class="s-rule-desc">征求党内外意见 · 政治审查 · 培训24学时 · 纪检会签</div>
                <div class="s-stat-row">
                  <span class="s-count text-gold">6<small>人</small></span>
                  <span class="s-tag tag-gold">严把关</span>
                </div>
              </div>

              <div class="stage-flow-card">
                <div class="s-card-top">
                  <span class="s-idx">04</span>
                  <span class="s-name">预备党员接收</span>
                </div>
                <div class="s-step-range">第 15~21 步</div>
                <div class="s-rule-desc">公示5工作日 · 大会票决 · 专人谈话 · 总支审批</div>
                <div class="s-stat-row">
                  <span class="s-count text-red">5<small>人</small></span>
                  <span class="s-tag tag-red">待审批</span>
                </div>
              </div>

              <div class="stage-flow-card">
                <div class="s-card-top">
                  <span class="s-idx">05</span>
                  <span class="s-name">考察期满转正</span>
                </div>
                <div class="s-step-range">第 22~25 步</div>
                <div class="s-rule-desc">预备期整1年 · 提交申请 · 讨论表决 · 档案归档</div>
                <div class="s-stat-row">
                  <span class="s-count text-green">7<small>人</small></span>
                  <span class="s-tag tag-green">已转正</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 卡片 4: 3 家子公司党支部发展推进督办台账 (横向3列舒展) -->
        <div class="dashboard-card card-branch-matrix">
          <div class="card-head">
            <div class="head-title">
              <span class="title-bar"></span>
              <h3>三家子公司党支部发展计划与程序执行督办</h3>
            </div>
            <span class="head-sub">支部责任落实 100%</span>
          </div>

          <div class="card-body">
            <div class="branch-3col-grid">
              <!-- 红数信息 -->
              <div class="branch-col-card">
                <div class="bcc-head">
                  <div class="bcc-name-line">
                    <strong>红数信息党支部</strong>
                    <span class="bcc-badge badge-amber">考察审批中</span>
                  </div>
                  <span class="bcc-sub">主营：云服务、运维服务、网络及安全服务</span>
                </div>
                <div class="bcc-data-grid">
                  <div class="bdg-item"><span class="bdg-lbl">年度核定</span><span class="bdg-val">4 人</span></div>
                  <div class="bdg-item"><span class="bdg-lbl">当前办结</span><span class="bdg-val text-amber">3 人 (75%)</span></div>
                  <div class="bdg-item"><span class="bdg-lbl">双培养骨干</span><span class="bdg-val">3 人入库</span></div>
                  <div class="bdg-item"><span class="bdg-lbl">先锋示范岗</span><span class="bdg-val">2 个设立</span></div>
                </div>
                <div class="bcc-progress"><div class="bcc-bar bar-amber" style="width: 75%"></div></div>
              </div>

              <!-- 幂次科技 -->
              <div class="branch-col-card">
                <div class="bcc-head">
                  <div class="bcc-name-line">
                    <strong>幂次科技党支部</strong>
                    <span class="bcc-badge badge-green">100% 满额达标</span>
                  </div>
                  <span class="bcc-sub">主营：软件开发、平台运营、企业数字化转型支撑</span>
                </div>
                <div class="bcc-data-grid">
                  <div class="bdg-item"><span class="bdg-lbl">年度核定</span><span class="bdg-val">5 人</span></div>
                  <div class="bdg-item"><span class="bdg-lbl">当前办结</span><span class="bdg-val text-green">5 人 (100%)</span></div>
                  <div class="bdg-item"><span class="bdg-lbl">双培养骨干</span><span class="bdg-val">4 人入库</span></div>
                  <div class="bdg-item"><span class="bdg-lbl">先锋示范岗</span><span class="bdg-val">3 个设立</span></div>
                </div>
                <div class="bcc-progress"><div class="bcc-bar bar-green" style="width: 100%"></div></div>
              </div>

              <!-- 链达科技 -->
              <div class="branch-col-card">
                <div class="bcc-head">
                  <div class="bcc-name-line">
                    <strong>链达科技党支部</strong>
                    <span class="bcc-badge badge-amber">转正催办中</span>
                  </div>
                  <span class="bcc-sub">主营：城市综合体运营等</span>
                </div>
                <div class="bcc-data-grid">
                  <div class="bdg-item"><span class="bdg-lbl">年度核定</span><span class="bdg-val">4 人</span></div>
                  <div class="bdg-item"><span class="bdg-lbl">当前办结</span><span class="bdg-val text-amber">3 人 (75%)</span></div>
                  <div class="bdg-item"><span class="bdg-lbl">双培养骨干</span><span class="bdg-val">2 人入库</span></div>
                  <div class="bdg-item"><span class="bdg-lbl">先锋示范岗</span><span class="bdg-val">1 个设立</span></div>
                </div>
                <div class="bcc-progress"><div class="bcc-bar bar-amber" style="width: 75%"></div></div>
              </div>
            </div>
          </div>
        </div>

        <!-- 卡片 5: 集团党总支 5 名委员政治履职与挂钩督导矩阵 -->
        <div class="dashboard-card card-committee-leadership">
          <div class="card-head">
            <div class="head-title">
              <span class="title-bar"></span>
              <h3>集团党总支 5 名委员政治履职与联系督导机制</h3>
            </div>
            <span class="head-sub">党总支统一领导 · 班子履职率 100%</span>
          </div>

          <div class="card-body">
            <div class="committee-5col-grid">
              <div v-for="(member, idx) in GENERAL_BRANCH_COMMITTEE" :key="idx" class="comm-member-tile">
                <div class="cmt-top">
                  <div class="cmt-avatar">{{ member.name.slice(0, 1) }}</div>
                  <div class="cmt-name-box">
                    <strong>{{ member.name }}</strong>
                    <span class="cmt-post">{{ member.post }}</span>
                  </div>
                </div>
                <div class="cmt-job">{{ member.job }}</div>
                <div class="cmt-duty">{{ member.duty }}</div>
                <div class="cmt-contact">
                  <span class="contact-lbl">督导联系：</span>
                  <span class="contact-val">{{ member.contactBranch }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- ----------------------------------------------------------------------- -->
      <!-- 右列 (28%)：党纪监督与双培养工程                                         -->
      <!-- ----------------------------------------------------------------------- -->
      <aside class="grid-col col-right">
        <!-- 卡片 6: 党纪党规硬性把关与合规防错引擎实时督办 -->
        <div class="dashboard-card card-compliance-radar">
          <div class="card-head">
            <div class="head-title">
              <span class="title-bar"></span>
              <h3>党纪监督与合规防错引擎实时督办</h3>
            </div>
            <span class="head-sub">把纪律和规矩挺在前面</span>
          </div>

          <div class="card-body">
            <div class="radar-summary-strip">
              <span class="rs-pill pill-red"><strong>1</strong> 件硬阻断拦截</span>
              <span class="rs-pill pill-amber"><strong>2</strong> 件时限亮牌督办</span>
              <span class="rs-pill pill-blue"><strong>1</strong> 件纪检廉政会签</span>
            </div>

            <div class="supervision-structured-list">
              <div class="supervision-item item-red">
                <div class="si-head">
                  <span class="si-tag tag-block">硬阻断生效</span>
                  <span class="si-cite">《细则》第十三条</span>
                </div>
                <div class="si-subject">张强（红数信息支部）· 入党积极分子</div>
                <div class="si-reason">
                  确定积极分子时间仅 <strong>290 天</strong>（法定需满 365 天）。系统强制锁死，禁止提前讨论列为发展对象，严防程序缩水违规入党。
                </div>
              </div>

              <div class="supervision-item item-amber">
                <div class="si-head">
                  <span class="si-tag tag-warn">谈话时限督办</span>
                  <span class="si-cite">《细则》第八条</span>
                </div>
                <div class="si-subject">陈思佳（集团综合管理部）· 递交申请第 22 天</div>
                <div class="si-reason">
                  距离“1个月内派人谈话”红线仅剩 <strong>8 天</strong>。已向支部书记亮牌督办，需按规程出具《谈话记录表》。
                </div>
              </div>

              <div class="supervision-item item-blue">
                <div class="si-head">
                  <span class="si-tag tag-info">纪检廉政会签</span>
                  <span class="si-cite">国企工作条例</span>
                </div>
                <div class="si-subject">林雨涵（幂次科技支部）· 拟接收预备党员</div>
                <div class="si-reason">
                  政治审查函调已齐备，当前流转至集团纪委出具《廉洁从业意见书》，严把廉洁关口，杜绝带病入党。
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 卡片 7: 国企“双培养”机制成效矩阵 -->
        <div class="dashboard-card card-dual-cultivate">
          <div class="card-head">
            <div class="head-title">
              <span class="title-bar"></span>
              <h3>国企“双培养”先锋工程成效矩阵</h3>
            </div>
            <span class="head-sub">双导师帮带指导</span>
          </div>

          <div class="card-body">
            <div class="dual-2x2-grid">
              <div class="dual-stat-tile">
                <div class="dst-val text-gold">10 <small>人</small></div>
                <div class="dst-title">业务骨干培养成党员</div>
                <div class="dst-desc">主营业务核心研发与运营骨干</div>
              </div>

              <div class="dual-stat-tile">
                <div class="dst-val text-gold">8 <small>人</small></div>
                <div class="dst-title">骨干党员成业务领头人</div>
                <div class="dst-desc">牵头重点项目攻关与运营突破</div>
              </div>

              <div class="dual-stat-tile">
                <div class="dst-val text-green">100<small>%</small></div>
                <div class="dst-title">双导师指导覆盖率</div>
                <div class="dst-desc">业务技术导师 + 支部党务骨干</div>
              </div>

              <div class="dual-stat-tile">
                <div class="dst-val text-white">6 <small>个</small></div>
                <div class="dst-title">党员先锋示范岗设立</div>
                <div class="dst-desc">急难险重岗位亮身份创实绩</div>
              </div>
            </div>

            <div class="org-life-status-bar">
              <div class="ols-item">
                <span class="ols-dot dot-green"></span>
                <span>三家子公司党支部“三会一课”规范开展率 <strong>100%</strong></span>
              </div>
              <div class="ols-item">
                <span class="ols-dot dot-green"></span>
                <span>党员党费按月规范收缴归档率 <strong>100%</strong></span>
              </div>
            </div>
          </div>
        </div>
      </aside>
    </section>

    <!-- ========================================================================= -->
    <!-- 4. 【新增两大高价值模块：上下排布！通栏拉长横向铺开，彻底拉伸填满下半部】     -->
    <!-- 模块一 (上)：三家子公司党支部“三会一课”规范督查台账 (通栏横向拉长，3大子公司展开)-->
    <!-- 模块二 (下)：围绕主营业务设立的 4 大“党员先锋示范岗”实战阵地 (通栏横向拉长，4大先锋岗展开)-->
    <!-- ========================================================================= -->
    
    <!-- 模块一 (上)：三家子公司党支部“三会一课”与每月主题党日规范督查台账 (通栏拉长) -->
    <section class="dashboard-card stacked-fullwidth-card">
      <div class="card-head">
        <div class="head-title">
          <span class="title-bar"></span>
          <h3>三家子公司党支部“三会一课”与每月主题党日规范督查台账</h3>
        </div>
        <span class="head-sub">巡视巡察必核事项 · 规程合规率 100% · 平均到会率 99.1% · 纪要归档率 100%</span>
      </div>

      <div class="card-body">
        <div class="stacked-subsidiary-grid">
          <!-- 红数信息 -->
          <div class="sub-branch-strip-card">
            <div class="sbsc-left">
              <div class="sbsc-title-line">
                <strong>红数信息党支部</strong>
                <span class="sbsc-status-pill">合规率 100%</span>
              </div>
              <span class="sbsc-biz-desc">【主营业务】云服务、运维服务、网络及安全服务</span>
              <div class="sbsc-counts-row">
                <span class="count-tag">支委会：<strong>12/12 次</strong></span>
                <span class="count-tag">党员大会：<strong>4/4 次</strong></span>
                <span class="count-tag">专题党课：<strong>4/4 次</strong></span>
                <span class="count-tag highlight-gold">主题党日：<strong>12/12 期</strong></span>
              </div>
            </div>
            <div class="sbsc-right">
              <div class="sbsc-log-title"><el-icon><Calendar /></el-icon> 主题党日与业务融合特色纪实：</div>
              <p class="sbsc-log-text">围绕云服务安全运维开展“筑牢网络安全底座·党员先锋亮剑重保”主题党日活动，组织支部全体党员立下保密与安全军令状，形成技术重保防线清单 14 条。</p>
            </div>
          </div>

          <!-- 幂次科技 -->
          <div class="sub-branch-strip-card">
            <div class="sbsc-left">
              <div class="sbsc-title-line">
                <strong>幂次科技党支部</strong>
                <span class="sbsc-status-pill">合规率 100%</span>
              </div>
              <span class="sbsc-biz-desc">【主营业务】软件开发、平台运营、企业数字化转型支撑</span>
              <div class="sbsc-counts-row">
                <span class="count-tag">支委会：<strong>12/12 次</strong></span>
                <span class="count-tag">党员大会：<strong>4/4 次</strong></span>
                <span class="count-tag">专题党课：<strong>4/4 次</strong></span>
                <span class="count-tag highlight-gold">主题党日：<strong>12/12 期</strong></span>
              </div>
            </div>
            <div class="sbsc-right">
              <div class="sbsc-log-title"><el-icon><Calendar /></el-icon> 主题党日与业务融合特色纪实：</div>
              <p class="sbsc-log-text">结合核心软件自研攻坚开展“党建赋能新质生产力·自主软件攻关誓师”主题党日活动，成立党员攻坚先锋突击专班，全力保障红河州重点产业企业数字化转型平台如期上线。</p>
            </div>
          </div>

          <!-- 链达科技 -->
          <div class="sub-branch-strip-card">
            <div class="sbsc-left">
              <div class="sbsc-title-line">
                <strong>链达科技党支部</strong>
                <span class="sbsc-status-pill">合规率 100%</span>
              </div>
              <span class="sbsc-biz-desc">【主营业务】城市综合体运营等</span>
              <div class="sbsc-counts-row">
                <span class="count-tag">支委会：<strong>12/12 次</strong></span>
                <span class="count-tag">党员大会：<strong>4/4 次</strong></span>
                <span class="count-tag">专题党课：<strong>4/4 次</strong></span>
                <span class="count-tag highlight-gold">主题党日：<strong>12/12 期</strong></span>
              </div>
            </div>
            <div class="sbsc-right">
              <div class="sbsc-log-title"><el-icon><Calendar /></el-icon> 主题党日与业务融合特色纪实：</div>
              <p class="sbsc-log-text">聚焦城市综合体高品质精细化运营，开展“党员亮身份·服务零距离·树城市综合体运营新标杆”实践活动，党员业务骨干带头深入一线驻场服务商户群众，解决难点堵点 28 个。</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 模块二 (下)：紧扣主营业务设立的 4 大“党员先锋示范岗”实战阵地 (通栏拉长) -->
    <section class="dashboard-card stacked-fullwidth-card">
      <div class="card-head">
        <div class="head-title">
          <span class="title-bar"></span>
          <h3>紧扣各自主营业务设立的 4 大“党员先锋示范岗”实战阵地矩阵</h3>
        </div>
        <span class="head-sub">把党组织堡垒建在项目最前沿 · 党旗在生产经营一线高高飘扬</span>
      </div>

      <div class="card-body">
        <div class="stacked-pioneer-grid">
          <!-- 先锋岗 1: 红数信息 -->
          <div class="stacked-pioneer-card">
            <div class="spc-top">
              <span class="spc-badge badge-p-red">重点先锋突击岗</span>
              <strong>云服务与网络安全重保党员突击队</strong>
            </div>
            <div class="spc-leader">红数信息党支部 · 支部书记李卫民领衔</div>
            <div class="spc-biz-line">【主营业务】云服务、运维服务、网络及安全服务</div>
            <p class="spc-desc">全天候保障红河州政务云与企业级核心系统运行安全，7×24 小时高等级网络防护响应，实现连续 1,800 天安全运行零事故。</p>
          </div>

          <!-- 先锋岗 2: 幂次科技 -->
          <div class="stacked-pioneer-card">
            <div class="spc-top">
              <span class="spc-badge badge-p-gold">自主软件攻坚区</span>
              <strong>软件开发与企业数字化转型支撑示范岗</strong>
            </div>
            <div class="spc-leader">幂次科技党支部 · 支部书记刘建华、赵丽领衔</div>
            <div class="spc-biz-line">【主营业务】软件开发、平台运营、企业数字化转型支撑</div>
            <p class="spc-desc">带领 6 名党员与骨干攻坚自主可控软件架构研发，持续优化重点平台运营，深度支撑红河州 50+ 企业数字化转型落地。</p>
          </div>

          <!-- 先锋岗 3: 链达科技 -->
          <div class="stacked-pioneer-card">
            <div class="spc-top">
              <span class="spc-badge badge-p-cyan">卓越品质示范岗</span>
              <strong>城市综合体智慧运营与便民服务示范哨</strong>
            </div>
            <div class="spc-leader">链达科技党支部 · 支部书记陈明、刘振华领衔</div>
            <div class="spc-biz-line">【主营业务】城市综合体运营等</div>
            <p class="spc-desc">高标准推行城市综合体智能化、精细化与绿色运营，设立党员先锋服务哨位，实现商户满意度 99.6%，打造城市空间品质典范。</p>
          </div>

          <!-- 先锋岗 4: 集团党总支 -->
          <div class="stacked-pioneer-card">
            <div class="spc-top">
              <span class="spc-badge badge-p-green">战略引领统筹岗</span>
              <strong>党总支党建与生产经营融合领航示范岗</strong>
            </div>
            <div class="spc-leader">集团党总支 · 朱文华书记、李建忠副书记统筹</div>
            <div class="spc-biz-line">【统领全局】全面加强党的领导，统领三家子公司高质量发展</div>
            <p class="spc-desc">把方向、管大局、保落实，5 名党总支委员挂钩督导三家子公司主营业务，将“把骨干培成党员、把党员培成骨干”政治责任落实落地。</p>
          </div>
        </div>
      </div>
    </section>

    <!-- ========================================================================= -->
    <!-- 5. 底部版本与庄严基座状态栏 (保持在最底部，稳如泰山)                         -->
    <!-- ========================================================================= -->
    <footer class="formal-footer-anchor">
      <div class="footer-left">
        <span class="quote-sign">“</span>
        <span>坚持党的领导、加强党的建设是我国国有企业的光荣传统，是国有企业的‘根’和‘魂’</span>
        <span class="quote-sign">”</span>
      </div>
      <div class="footer-center">
        <span>红河数据产业集团有限公司总支部委员会 · 智慧党建数字化调度指挥大屏 V1.5</span>
      </div>
      <div class="footer-right">
        <span>党风廉政监督热线与纪检信箱公示在线受控</span>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import PartyEmblem from './PartyEmblem.vue'
import { ALL_MEMBERS_ROSTER, GENERAL_BRANCH_COMMITTEE } from '../data/mockData.js'

defineEmits(['close'])

// 时间格式化
const liveTimeStr = ref('')
const liveDateStr = ref('')
let timer = null

function updateClock() {
  const now = new Date()
  const days = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  liveTimeStr.value = String(now.getHours()).padStart(2, '0') + ':' + 
    String(now.getMinutes()).padStart(2, '0') + ':' + 
    String(now.getSeconds()).padStart(2, '0')
  liveDateStr.value = now.getFullYear() + '年' + (now.getMonth() + 1) + '月' + now.getDate() + '日 ' + days[now.getDay()]
}

// 统计量
const totalCount = computed(() => ALL_MEMBERS_ROSTER.length)
const formalCount = computed(() => ALL_MEMBERS_ROSTER.filter(m => m.partyStatus === 1).length)
const probationaryCount = computed(() => ALL_MEMBERS_ROSTER.filter(m => m.partyStatus === 2).length)
const developingCount = computed(() => ALL_MEMBERS_ROSTER.filter(m => m.partyStatus === 3 || m.partyStatus === 4).length)

// 图表 DOM
const statusDonutRef = ref(null)
const standingBarRef = ref(null)
let statusDonutChart = null
let standingBarChart = null

function initCharts() {
  // 1. 结构环形图
  if (statusDonutRef.value) {
    statusDonutChart = echarts.init(statusDonutRef.value)
    statusDonutChart.setOption({
      backgroundColor: 'transparent',
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c}人 ({d}%)',
        backgroundColor: '#260405',
        borderColor: '#a31517',
        textStyle: { color: '#f5f5f5', fontSize: 12 }
      },
      legend: {
        orient: 'vertical',
        right: '2%',
        top: 'center',
        itemWidth: 10,
        itemHeight: 10,
        itemGap: 10,
        textStyle: { color: '#e5dac1', fontSize: 11.5 },
        formatter: (name) => {
          const map = { 
            '正式党员': `${formalCount.value}人`, 
            '预备党员': `${probationaryCount.value}人`, 
            '发展对象': '1人', 
            '积极分子': '1人' 
          }
          return `${name}  ${map[name] || ''}`
        }
      },
      series: [
        {
          name: '队伍政治面貌',
          type: 'pie',
          radius: ['48%', '75%'],
          center: ['34%', '50%'],
          itemStyle: {
            borderColor: '#240405',
            borderWidth: 2
          },
          label: { show: false },
          data: [
            { value: formalCount.value, name: '正式党员', itemStyle: { color: '#a31517' } },
            { value: probationaryCount.value, name: '预备党员', itemStyle: { color: '#c59b27' } },
            { value: 1, name: '发展对象', itemStyle: { color: '#8c6d31' } },
            { value: 1, name: '积极分子', itemStyle: { color: '#4a7c59' } }
          ]
        }
      ]
    })
  }

  // 2. 党龄分布条形图
  if (standingBarRef.value) {
    standingBarChart = echarts.init(standingBarRef.value)
    standingBarChart.setOption({
      backgroundColor: 'transparent',
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        backgroundColor: '#260405',
        borderColor: '#c59b27',
        textStyle: { color: '#f5f5f5' }
      },
      grid: {
        top: '16%',
        left: '3%',
        right: '4%',
        bottom: '8%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: ['1~3年', '3~5年', '5~10年', '10~15年', '15年以上'],
        axisLine: { lineStyle: { color: 'rgba(212, 175, 55, 0.25)' } },
        axisLabel: { color: '#d1c4a5', fontSize: 11 }
      },
      yAxis: {
        type: 'value',
        splitLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.06)' } },
        axisLabel: { color: '#d1c4a5', fontSize: 11 }
      },
      series: [
        {
          name: '党员人数',
          type: 'bar',
          barWidth: '22',
          data: [
            { value: 2, itemStyle: { color: '#8c6d31' } },
            { value: 2, itemStyle: { color: '#a68239' } },
            { value: 3, itemStyle: { color: '#c59b27' } },
            { value: 4, itemStyle: { color: '#a31517' } },
            { value: 4, itemStyle: { color: '#d4af37' } }
          ],
          label: {
            show: true,
            position: 'top',
            color: '#f5e8c7',
            fontSize: 11,
            formatter: '{c}人'
          }
        }
      ]
    })
  }
}

function handleResize() {
  if (statusDonutChart) statusDonutChart.resize()
  if (standingBarChart) standingBarChart.resize()
}

onMounted(() => {
  updateClock()
  timer = setInterval(updateClock, 1000)
  nextTick(() => {
    initCharts()
    window.addEventListener('resize', handleResize)
  })
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  window.removeEventListener('resize', handleResize)
  if (statusDonutChart) statusDonutChart.dispose()
  if (standingBarChart) standingBarChart.dispose()
})
</script>

<style scoped>
/* 全屏容器：故宫正统庄重深红木色，纵向舒展，不留空隙 */
.solemn-screen-container {
  position: fixed;
  inset: 0;
  z-index: 3000;
  background-color: #240405;
  background-image: 
    linear-gradient(180deg, #350507 0%, #220304 40%, #150202 100%);
  color: #f7f7f7;
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
  padding: 12px 24px;
  overflow-y: auto;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* ========================================================================= */
/* 1. 顶部庄重大气抬头                                                       */
/* ========================================================================= */
.formal-header {
  margin-bottom: 2px;
}

.header-inner {
  display: grid;
  grid-template-columns: 340px 1fr 340px;
  align-items: center;
  min-height: 56px;
}

.header-meta-left {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.org-affiliation {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #fce7b0;
}

.org-dot {
  width: 7px;
  height: 7px;
  background: #d4af37;
  border-radius: 50%;
}

.live-datetime {
  font-size: 11.5px;
  color: #c9b996;
}

.time-bold {
  font-family: monospace;
  font-weight: 700;
  margin-left: 6px;
  color: #ffeb99;
}

.header-center-title {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.title-emblem-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.cpc-flag-badge {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.title-emblem-row h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  letter-spacing: 1.5px;
  color: #fef3c7;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.7);
}

.subtitle-text {
  font-size: 11.5px;
  color: #c9b996;
  margin-top: 2px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.sub-sep {
  color: #d4af37;
}

.header-meta-right {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 14px;
}

.compliance-badge {
  font-size: 11.5px;
  color: #86efac;
  background: rgba(34, 197, 94, 0.12);
  border: 1px solid rgba(34, 197, 94, 0.3);
  padding: 4px 10px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.shield-icon {
  display: flex;
  align-items: center;
  color: #4ade80;
}

.exit-btn {
  background: #4a080a;
  border: 1px solid #c59b27;
  color: #fef0c7;
  padding: 5px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: all 0.2s;
}

.exit-btn:hover {
  background: #730e10;
}

.header-divider {
  height: 2px;
  width: 100%;
  background: linear-gradient(90deg, transparent 0%, #c59b27 25%, #d4af37 50%, #c59b27 75%, transparent 100%);
  margin-top: 5px;
}

/* ========================================================================= */
/* 2. 核心 KPI 汇总排                                                         */
/* ========================================================================= */
.kpi-banner-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 10px;
}

.kpi-box {
  background: rgba(45, 6, 8, 0.9);
  border: 1px solid rgba(212, 175, 55, 0.3);
  border-radius: 4px;
  padding: 9px 14px;
  display: flex;
  flex-direction: column;
}

.kpi-content {
  display: flex;
  flex-direction: column;
}

.kpi-label {
  font-size: 11.5px;
  color: #c9b996;
  margin-bottom: 2px;
}

.kpi-val-row {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-bottom: 2px;
}

.kpi-val {
  font-size: 25px;
  font-weight: 800;
  line-height: 1;
}

.kpi-unit {
  font-size: 11.5px;
  color: #c9b996;
}

.text-xs {
  font-size: 13px;
  font-weight: 600;
  color: #d1c4a5;
}

.kpi-badge {
  margin-left: auto;
  font-size: 10.5px;
  color: #d4af37;
  background: rgba(212, 175, 55, 0.12);
  border: 1px solid rgba(212, 175, 55, 0.25);
  padding: 1px 6px;
  border-radius: 3px;
}

.badge-amber { color: #f59e0b; background: rgba(245, 158, 11, 0.12); border-color: rgba(245, 158, 11, 0.25); }
.badge-gold { color: #facc15; background: rgba(250, 204, 21, 0.12); border-color: rgba(250, 204, 21, 0.25); }
.badge-green { color: #4ade80; background: rgba(74, 222, 128, 0.12); border-color: rgba(74, 222, 128, 0.25); }

.kpi-note {
  font-size: 10px;
  color: #8c7f69;
}

.gold-num { color: #facc15; }
.white-num { color: #f8fafc; }
.amber-num { color: #fbbf24; }

/* ========================================================================= */
/* 3. 主体三列黄金排版 (完全恢复经典庄严布局)                                 */
/* ========================================================================= */
.dashboard-body-grid {
  display: grid;
  grid-template-columns: 3.1fr 4.4fr 3.1fr;
  gap: 12px;
}

.grid-col {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dashboard-card {
  background: rgba(40, 6, 8, 0.92);
  border: 1px solid rgba(212, 175, 55, 0.28);
  border-radius: 4px;
  padding: 10px 14px;
  display: flex;
  flex-direction: column;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 6px;
  border-bottom: 1px solid rgba(212, 175, 55, 0.2);
  margin-bottom: 8px;
}

.head-title {
  display: flex;
  align-items: center;
  gap: 6px;
}

.title-bar {
  width: 4px;
  height: 13px;
  background: #c59b27;
  border-radius: 2px;
}

.head-title h3 {
  margin: 0;
  font-size: 13.5px;
  font-weight: 700;
  color: #fef3c7;
}

.head-sub {
  font-size: 11px;
  color: #a89a80;
}

.card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* 左列图表 */
.donut-chart-box {
  height: 140px;
  width: 100%;
}

.echart-inner {
  height: 100%;
  width: 100%;
}

.echart-bar-inner {
  height: 165px;
  width: 100%;
}

.quota-check-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: 4px;
}

.check-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.ci-head {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #e2d7be;
}

.ci-stat strong {
  color: #fef3c7;
}

.text-green { color: #4ade80 !important; }
.text-amber { color: #fbbf24 !important; }
.text-gold { color: #facc15 !important; }
.text-red { color: #f87171 !important; }
.text-blue { color: #60a5fa !important; }

.ci-track {
  height: 5px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 3px;
  overflow: hidden;
}

.ci-bar {
  height: 100%;
  background: #c59b27;
  border-radius: 3px;
}

/* 中列：5大阶段 + 3家子公司 + 5名党总支委员履职矩阵 */
.stages-flow-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
  margin-top: 2px;
}

.stage-flow-card {
  background: rgba(28, 4, 6, 0.85);
  border: 1px solid rgba(212, 175, 55, 0.22);
  border-top: 3px solid #8b1315;
  border-radius: 3px;
  padding: 7px 9px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 145px;
}

.stage-flow-card:nth-child(2) { border-top-color: #c59b27; }
.stage-flow-card:nth-child(3) { border-top-color: #8c6d31; }
.stage-flow-card:nth-child(4) { border-top-color: #a31517; }
.stage-flow-card:nth-child(5) { border-top-color: #4a7c59; background: rgba(38, 5, 8, 0.95); }

.s-card-top {
  display: flex;
  align-items: baseline;
  gap: 5px;
  margin-bottom: 2px;
}

.s-idx {
  font-size: 12.5px;
  font-weight: 800;
  color: #c59b27;
}

.s-name {
  font-size: 12px;
  font-weight: 700;
  color: #f8fafc;
}

.s-step-range {
  font-size: 10px;
  color: #8c7f69;
  margin-bottom: 3px;
}

.s-rule-desc {
  font-size: 10.5px;
  color: #c4b595;
  line-height: 1.4;
  flex: 1;
}

.s-stat-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-top: 5px;
  padding-top: 4px;
  border-top: 1px dashed rgba(255, 255, 255, 0.1);
}

.s-count {
  font-size: 16px;
  font-weight: 800;
  color: #f8fafc;
}

.s-count small {
  font-size: 10.5px;
  font-weight: normal;
}

.s-tag {
  font-size: 10px;
  color: #c9b996;
  background: rgba(255, 255, 255, 0.08);
  padding: 1px 5px;
  border-radius: 2px;
}

.tag-amber { color: #f59e0b; background: rgba(245, 158, 11, 0.15); }
.tag-gold { color: #facc15; background: rgba(250, 204, 21, 0.15); }
.tag-red { color: #f87171; background: rgba(239, 68, 68, 0.15); }
.tag-green { color: #4ade80; background: rgba(74, 222, 128, 0.15); }

/* 下部 3 家子公司支部卡片 (3 列布局) */
.branch-3col-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.branch-col-card {
  background: rgba(28, 4, 6, 0.85);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 3px;
  padding: 8px 10px;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.bcc-name-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.bcc-name-line strong {
  font-size: 12px;
  color: #fef3c7;
}

.bcc-badge {
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 2px;
}

.bcc-sub {
  font-size: 10px;
  color: #8c7f69;
  display: block;
  margin-top: 1px;
}

.bcc-data-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 3px;
  background: rgba(0, 0, 0, 0.25);
  padding: 5px 6px;
  border-radius: 3px;
}

.bdg-item {
  display: flex;
  flex-direction: column;
}

.bdg-lbl {
  font-size: 9.5px;
  color: #8c7f69;
}

.bdg-val {
  font-size: 11px;
  font-weight: 700;
  color: #e2d7be;
}

.bcc-progress {
  height: 4px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 2px;
  overflow: hidden;
}

.bcc-bar {
  height: 100%;
}

/* 党总支 5 名委员履职尽责矩阵 */
.committee-5col-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
}

.comm-member-tile {
  background: rgba(28, 4, 6, 0.85);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 3px;
  padding: 7px 8px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.cmt-top {
  display: flex;
  align-items: center;
  gap: 5px;
}

.cmt-avatar {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #8e1012;
  color: #fef3c7;
  border: 1px solid #d4af37;
  font-size: 10.5px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cmt-name-box {
  display: flex;
  flex-direction: column;
}

.cmt-name-box strong {
  font-size: 11.5px;
  color: #f8fafc;
}

.cmt-post {
  font-size: 9.5px;
  color: #facc15;
}

.cmt-job {
  font-size: 10px;
  color: #c9b996;
}

.cmt-duty {
  font-size: 9.5px;
  color: #948670;
  line-height: 1.35;
  margin: 1px 0;
  flex: 1;
}

.cmt-contact {
  display: flex;
  font-size: 9.5px;
  padding-top: 3px;
  border-top: 1px dashed rgba(255, 255, 255, 0.1);
}

.contact-lbl {
  color: #8c7f69;
}

.contact-val {
  color: #4ade80;
  font-weight: 600;
}

/* 右列：合规雷达与双培养工程 */
.radar-summary-strip {
  display: flex;
  justify-content: space-between;
  gap: 6px;
  margin-bottom: 7px;
}

.rs-pill {
  flex: 1;
  text-align: center;
  font-size: 10px;
  padding: 3px 4px;
  border-radius: 3px;
}

.pill-red { background: rgba(239, 68, 68, 0.12); color: #f87171; border: 1px solid rgba(239, 68, 68, 0.25); }
.pill-amber { background: rgba(245, 158, 11, 0.12); color: #fbbf24; border: 1px solid rgba(245, 158, 11, 0.25); }
.pill-blue { background: rgba(59, 130, 246, 0.12); color: #60a5fa; border: 1px solid rgba(59, 130, 246, 0.25); }

.supervision-structured-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.supervision-item {
  background: rgba(28, 4, 6, 0.9);
  border-radius: 3px;
  padding: 6px 9px;
  border-left: 3px solid #8b1315;
}

.item-red { border-left-color: #ef4444; border: 1px solid rgba(239, 68, 68, 0.2); border-left-width: 3px; }
.item-amber { border-left-color: #f59e0b; }
.item-blue { border-left-color: #3b82f6; }

.si-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2px;
}

.si-tag {
  font-size: 10px;
  font-weight: 700;
}

.tag-block { color: #f87171; }
.tag-warn { color: #fbbf24; }
.tag-info { color: #60a5fa; }

.si-cite {
  font-size: 9.5px;
  color: #8c7f69;
}

.si-subject {
  font-size: 11px;
  font-weight: 700;
  color: #f8fafc;
  margin-bottom: 1px;
}

.si-reason {
  font-size: 10px;
  color: #c4b595;
  line-height: 1.35;
}

.si-reason strong {
  color: #fef3c7;
}

.dual-2x2-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px;
}

.dual-stat-tile {
  background: rgba(28, 4, 6, 0.85);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 3px;
  padding: 6px 8px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.dst-val {
  font-size: 19px;
  font-weight: 800;
  line-height: 1;
  margin-bottom: 2px;
}

.dst-val small {
  font-size: 10.5px;
  font-weight: normal;
}

.dst-title {
  font-size: 11px;
  font-weight: 700;
  color: #f8fafc;
  margin-bottom: 1px;
}

.dst-desc {
  font-size: 9.5px;
  color: #8c7f69;
}

.org-life-status-bar {
  margin-top: 6px;
  padding-top: 6px;
  border-top: 1px dashed rgba(212, 175, 55, 0.2);
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.ols-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 10.5px;
  color: #c9b996;
}

.ols-item strong {
  color: #4ade80;
}

.ols-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
}

.dot-green { background: #4ade80; }

/* ========================================================================= */
/* 4. 【新增两大模块：上下排布！通栏拉长横向铺开，彻底拉伸填满下半部】        */
/* ========================================================================= */
.stacked-fullwidth-card {
  width: 100%;
  box-sizing: border-box;
}

/* 模块一 (上)：三家子公司“三会一课”台账 (3列通栏舒展拉长) */
.stacked-subsidiary-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.sub-branch-strip-card {
  background: rgba(28, 4, 6, 0.85);
  border: 1px solid rgba(212, 175, 55, 0.22);
  border-radius: 4px;
  padding: 10px 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sbsc-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sbsc-title-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sbsc-title-line strong {
  font-size: 13.5px;
  color: #fef3c7;
}

.sbsc-status-pill {
  font-size: 11px;
  color: #4ade80;
  background: rgba(74, 222, 128, 0.12);
  border: 1px solid rgba(74, 222, 128, 0.25);
  padding: 1px 6px;
  border-radius: 2px;
}

.sbsc-biz-desc {
  font-size: 11px;
  color: #c9b996;
  background: rgba(0, 0, 0, 0.25);
  padding: 2px 6px;
  border-radius: 2px;
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.sbsc-counts-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 4px;
  margin-top: 4px;
}

.count-tag {
  font-size: 11px;
  color: #cbd5e1;
  background: rgba(0, 0, 0, 0.3);
  padding: 3px 6px;
  border-radius: 2px;
  text-align: center;
}

.count-tag strong {
  color: #f8fafc;
}

.count-tag.highlight-gold strong {
  color: #facc15;
}

.sbsc-right {
  border-top: 1px dashed rgba(255, 255, 255, 0.1);
  padding-top: 6px;
}

.sbsc-log-title {
  font-size: 11px;
  color: #e5c158;
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 2px;
}

.sbsc-log-text {
  margin: 0;
  font-size: 10.5px;
  color: #a89a80;
  line-height: 1.45;
}

/* 模块二 (下)：4大党员先锋示范岗实战阵地 (4列通栏舒展拉长) */
.stacked-pioneer-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.stacked-pioneer-card {
  background: rgba(28, 4, 6, 0.85);
  border: 1px solid rgba(212, 175, 55, 0.22);
  border-radius: 4px;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.spc-top {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.spc-badge {
  font-size: 10px;
  font-weight: 700;
  padding: 1px 6px;
  border-radius: 2px;
  align-self: flex-start;
}

.spc-top strong {
  font-size: 12.5px;
  color: #f8fafc;
  line-height: 1.35;
  margin-top: 2px;
}

.badge-p-red { color: #f87171; background: rgba(239, 68, 68, 0.15); border: 1px solid rgba(239, 68, 68, 0.3); }
.badge-p-gold { color: #facc15; background: rgba(250, 204, 21, 0.15); border: 1px solid rgba(250, 204, 21, 0.3); }
.badge-p-cyan { color: #38bdf8; background: rgba(56, 189, 248, 0.15); border: 1px solid rgba(56, 189, 248, 0.3); }
.badge-p-green { color: #4ade80; background: rgba(74, 222, 128, 0.15); border: 1px solid rgba(74, 222, 128, 0.3); }

.spc-leader {
  font-size: 11px;
  color: #e5c158;
}

.spc-biz-line {
  font-size: 10.5px;
  color: #86efac;
  background: rgba(34, 197, 94, 0.1);
  padding: 2px 6px;
  border-radius: 2px;
  line-height: 1.35;
}

.spc-desc {
  margin: 0;
  font-size: 10.5px;
  color: #a89a80;
  line-height: 1.45;
  flex: 1;
}

/* ========================================================================= */
/* 5. 底部版本与庄严基座状态栏 (保持在最底部，稳如泰山)                         */
/* ========================================================================= */
.formal-footer-anchor {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(20, 2, 4, 0.95);
  border: 1px solid rgba(212, 175, 55, 0.25);
  border-radius: 3px;
  padding: 6px 14px;
  font-size: 11px;
  color: #8c7f69;
  margin-top: 2px;
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #c9b996;
}

.quote-sign {
  color: #d4af37;
  font-size: 13px;
  font-weight: 800;
}

.footer-center {
  color: #a89a80;
  font-weight: 600;
}

.footer-right {
  color: #86efac;
}
</style>
