---
name: apricity-ui
description: 为 Minecraft ApricityUI 生成 HTML/CSS/JS 界面代码
---

你的目标不是"写一个浏览器网页"，而是"为 Minecraft 里的 ApricityUI 生成能运行、能维护、别太离谱的 UI"。

请严格按下面的约束输出，别把浏览器习惯整包带进来。

## 1. 角色与目标

- 输出可直接在 ApricityUI 中使用的 HTML、CSS、JS
- 优先保证可运行，再考虑花活
- 遇到不确定能力时，使用保守方案，不要脑补未实现特性

## 2. 先记住这几条

- 默认只输出 `<body>...</body>`，不要写 `<html>`、`<head>`
- 不要依赖浏览器的 UA 默认样式，标题、段落、列表这类都要自己想清楚样式
- 容器相关统一使用 `container` + `slot` + `recipe` 的新语义，不要继续写旧模板
- 如果用户没要求解释，直接给完整结果，不要先讲一堆道理

## 3. 和浏览器最不一样的地方

- 标签能被解析，不代表拥有浏览器等价语义
- JS 运行环境不是完整 Web API，只能用文档里明确可用的那部分
- 超出实现范围的 CSS 往往会被忽略，所以不要赌"也许能生效"
- 资源路径优先按 ApricityUI 的资源层解析，不要假设存在浏览器站点根目录
- `querySelectorAll` / `children` / `childNodes` / `getElementsBy*` 返回 **JS 数组**，不是浏览器原生 NodeList/HTMLCollection

## 4. UI 类型与入口

### 4.1 Overlay（叠加层）

最轻量，适合 HUD、状态提示、调试面板。不阻塞当前界面。

```js
// KJS
let doc = ApricityUI.createDocument("demo/index.html")
ApricityUI.removeDocument("demo/index.html")

// Java
ApricityUI.createDocument("demo/index.html");
ApricityUI.removeDocument("demo/index.html");
```

### 4.2 Screen（界面）

标准界面，可带真实容器绑定。

纯预览（无容器绑定）：
```js
ApricityUI.openScreen("demo/index.html")
ApricityUI.closeScreen()
```

带容器绑定（服务端权威）：
```js
let plan = ApricityUI.bind()
    .primaryBind("main").savedData("apricityui_demo", "demo_key", 27)
    .bind("player").player()
    .build()

ApricityUI.openScreen(player, "demo/index.html", plan)
```

绑定类型：
- **玩家背包**：`.primaryBind("player").player()`
- **SavedData**：`.primaryBind("main").savedData("modid", "key", 27)`
- **方块实体**：`.primaryBind("machine").blockEntity(x, y, z, "up")`
- **实体背包**：`.primaryBind("entity_inv").entity("uuid")`

**关键**：模板顶层 `container` 的 `id` 必须和 `OpenBindPlan` 里的名字对上。

### 4.3 WorldWindow（世界内影像）

把 UI 渲染成世界里的一块平面，适合信息板、机器外屏。

```java
WorldWindow window = ApricityUI.createWorldWindow("demo/world.html", position, 180, 100, 16);
ApricityUI.removeWorldWindow(window);
```

默认缩放约 0.02f（约 50 像素 = 1 格方块）。

### 4.4 FollowFacingWorldWindow

在 WorldWindow 基础上跟随玩家视角并始终朝向摄像机。`followFactor` 范围 0~1（推荐 0.2~0.5）。

```java
FollowFacingWorldWindow window = ApricityUI.createFollowFacingWorldWindow(
    "demo/follow.html", position, 180, 100, 16, 0.3f
);
```

## 5. 可优先使用的标签

### 5.1 已注册标签

- 通用：`body` `div` `span` `pre` `img` `a` `input` `textarea` `select` `option` `sprite`
- Minecraft：`container` `slot` `recipe` `translation`

### 5.2 依赖 global.css 才比较像样的标签

- 文本结构：`p` `h1` `h2` `h3` `h4` `h5` `h6` `small`
- 强调类：`b` `strong` `i` `em` `mark`
- 行内变体：`sub` `sup` `code` `kbd`
- 分隔类：`hr` `blockquote`

能用，但别把它们当浏览器原生组件来赌细节。

## 6. Minecraft 专属标签规则

### 6.1 container

- 顶层容器建议显式写 `id`，方便和服务端 `OpenBindPlan` 对齐
- `bind="player"` 的容器如果没有显式写出 bound 槽位，会自动补玩家 36 格（27 格背包 + 9 格快捷栏，间距 4px）
- 若已显式声明 bound 槽位，不会触发隐式注入
- 标题不再读旧属性，容器标题只会读取**首个子元素**的文本内容（优先取首个子 `div` 文本，否则自动注入标题 div，回退到翻译键 `screen.apricityui.container.<menuKey>`）
- `layout` 属性定义槽位布局（如 `layout="[27,3,9]"` 或 `layout="preset:player"`），仅影响布局，不自动创建槽位实例

### 6.2 slot

统一使用 `<slot>`：
- `mode="bound"` — 绑定真实菜单槽位；`mode="virtual"` — 纯展示槽位
- 在 `container` 内默认 **bound**，容器外默认 **virtual**
- `repeat="36"` — 批量生成（总数语义，模板算第 0 个）
- **bound 常用**：`slot-index`、`disabled`
- **virtual 常用**：`item`、`count`、`hover`、`rotate-interval`（轮播间隔 ms）
- virtual 物品优先写在 `slot` 的 `innerText` 里：

```html
<slot>minecraft:diamond</slot>
<slot>#minecraft:planks</slot>
<slot>{id:"minecraft:diamond",Count:12b}</slot>
```

旧的 `item`、`itemid`、`count` 这类属性写法不要继续推荐（但 virtual slot 仍然支持 `item` 属性作为替代写法）。

### 6.3 recipe

```html
<recipe type="crafting_shaped">minecraft:crafting_table</recipe>
```

- `type` 必填
- 配方 id 从 `innerText` 读取，不再使用 `recipe-id`
- recipe 生成出来的槽位始终是 virtual，只用于展示，不参与真实容器绑定
- 容器内可放多个 `<recipe>`，每个独立子布局

### 6.4 translation

```html
<translation>item.minecraft.tropical_fish</translation>
```

`innerText` 就是翻译 key。

### 6.5 sprite

推荐只使用：`src` `steps` `duration` `direction` `loop` `steps-mode` `autoplay` `initialFrame` `fit`

不要要求额外提供 `frameW`、`frameH`。当前实现会根据图片尺寸和 `steps` 推导帧信息。

## 7. CSS 使用策略

### 7.1 常用选择器

标签、`.class`、`#id`、`[attr]`、`[attr=value]`、`:first-child` `:last-child` `:nth-child()` `:hover` `:active` `:focus` `:empty` `:checked`、后代空格、`>`、`,`

### 7.2 常用属性

- 布局：`display` `flex-*` `grid-template-*` `grid-row` `grid-column`
- 尺寸：`width` `height` `min/max-*`
- 盒模型：`margin*` `padding*` `border*` `border-radius` `border-image`
- 位置：`position` `top` `right` `bottom` `left` `z-index`
- 背景：`background-*`
- 文本：`color` `font-size` `font-family` `font-weight` `font-style` `line-height` `text-stroke`
- 视觉：`opacity` `box-shadow` `transform` `clip-path` `filter` `backdrop-filter`
- 交互：`cursor` `pointer-events` `visibility` `user-select`
- 动效：`transition` `animation*` `@keyframes` `@font-face`
- 变量：`--*`

写了没实现的属性，最常见的结果不是报错，而是**安静地没效果**。

### 7.3 槽位变量

`--aui-slot-size` `--aui-slot-render-bg` `--aui-slot-render-item` `--aui-slot-icon-scale` `--aui-slot-padding` `--aui-slot-z` `--aui-slot-interactive` `--aui-slot-cycle` `--aui-slot-cycle-interval` `--aui-container-columns`

## 8. JS 能力边界

### 8.1 全局对象

页面可直接使用：`window` `document` `console` `localStorage` `sessionStorage` `performance` `fetch` `getComputedStyle` `requestAnimationFrame` `cancelAnimationFrame` `setTimeout` `clearTimeout` `setInterval` `clearInterval` `Event` `CustomEvent` `MouseEvent` `WheelEvent` `PointerEvent` `ResizeObserver` `MutationObserver` `URLSearchParams` `FormData`

### 8.2 DOM 查询与操作（优先使用）

```js
// 查询
document.querySelector(".title")
document.querySelectorAll("slot")         // 返回 JS 数组
document.getElementById("main")
document.getElementsByClassName("item")   // 返回 JS 数组
document.getElementsByTagName("div")      // 返回 JS 数组
el.closest(".container")
el.matches(".active")
el.contains(otherEl)

// 创建与增删
document.createElement("div")
document.createTextNode("text")
el.append(child)        // 支持字符串、数字、布尔（自动转文本）
el.prepend(child)
el.before(child)
el.after(child)
el.replaceWith(child)
el.remove()
el.appendChild(child)
el.insertBefore(child, ref)
el.removeChild(child)

// 属性
el.getAttribute("class")
el.setAttribute("class", "panel")
el.removeAttribute("class")
el.hasAttribute("class")

// classList（轻量 DOMTokenList）
el.classList.add("active", "open")
el.classList.remove("active")
el.classList.toggle("collapsed")
el.classList.contains("active")
el.classList.length

// dataset（轻量 DOMStringMap，data-foo-bar ↔ dataset.fooBar）
el.dataset.state = "open"
el.dataset.get("state")
el.dataset.has("state")
el.dataset.delete("state")

// 内容与表单
el.innerText          // 推荐用于设置/读取文本
el.textContent
el.innerHTML          // 可用但注意不是完整浏览器实现
el.value              // input/textarea/select
el.checked            // checkbox/radio
el.selectedIndex      // select

// 遍历
el.children           // JS 数组
el.childNodes         // JS 数组（元素风格，非完整 Node 模型）
el.firstElementChild
el.lastElementChild
el.nextElementSibling
el.previousElementSibling
el.parentElement

// 布局
el.getBoundingClientRect()  // 返回 {x, y, width, height, left, top, right, bottom}
el.scrollTo(x, y)
el.scrollBy(x, y)

// 表单交互
el.focus()
el.blur()
el.click()
```

### 8.3 事件

```js
el.addEventListener("mousedown", (e) => { ... })
el.removeEventListener("mousedown", handler)
el.dispatchEvent(new Event("custom"))

// 可用事件类型
new Event(type, { bubbles: true })
new CustomEvent(type, { detail: value, bubbles: true })
new MouseEvent(type, { clientX, clientY, button })
new WheelEvent(type, { clientX, clientY, deltaX, deltaY, deltaMode })
new PointerEvent(type, { clientX, clientY, button, pointerId, pointerType, isPrimary })
```

事件对象字段：`type` `target` `currentTarget` `bubbles` `cancelable` `defaultPrevented`
方法：`stopPropagation()` `preventDefault()`

常用交互事件：`click` `dblclick` `contextmenu` `wheel` `scroll` `mousedown` `mouseup` `mousemove`
指针事件（mouse 兼容层）：`pointerdown` `pointerup` `pointermove` `pointerover` `pointerout` `pointerenter` `pointerleave`
生命周期：`DOMContentLoaded` `load`（实时派发，不补发历史事件）
img 事件：`load` `error`（默认不冒泡）

### 8.4 定时器与帧调度

```js
window.setTimeout(() => { ... }, 500)
window.setInterval(() => { ... }, 1000)
window.requestAnimationFrame(() => { ... })  // 约 16ms 一次
```

### 8.5 存储

```js
localStorage.setItem("theme", "dark")    // 持久化到本地配置目录
localStorage.getItem("theme")
localStorage.removeItem("theme")
localStorage.clear()

sessionStorage.setItem("key", "val")     // 仅当前运行期有效，不落盘
```

### 8.6 fetch（轻量 GET）

```js
fetch("path/to/data.json")
  .then(resp => resp.json())
  .then(data => console.log(data))
  .catch(err => console.error(err))

// Response: resp.ok, resp.status, resp.url
// 方法: resp.text(), resp.json(), resp.bytes()
```

**限制**：仅支持单参数 url，不支持 `fetch(url, init)`，不支持自定义 method/headers/body。主要用于读取本地或远程资源。

### 8.7 其他全局

```js
// console
console.log("msg")
console.warn("msg")
console.error("msg")
console.time("label")
console.timeEnd("label")

// performance
performance.now()

// getComputedStyle（轻量只读）
window.getComputedStyle(el).getPropertyValue("width")

// location（只读，assign/replace/reload 仅占位）
window.location.href
window.location.pathname
window.location.search

// URLSearchParams
new URLSearchParams("?foo=1&bar=2").get("foo")

// FormData（不支持文件上传）
new FormData(formEl).get("field")

// ResizeObserver（轻量，帧更新驱动）
new ResizeObserver((entries) => { ... }).observe(el)

// MutationObserver（轻量，按帧批量派发）
new MutationObserver((records) => { ... }).observe(el, { childList: true, attributes: true, subtree: true })
```

### 8.8 避免依赖

完整 BOM、`window.history`、`matchMedia`、`postMessage`、`WebSocket`、`XMLHttpRequest`、真实页面导航、完整 Node/Text/Comment 模型、完整 HTMLCollection/NodeList 行为

## 9. 资源系统

### 9.1 资源路径与优先级

三层覆盖（优先级从高到低）：
1. 开发环境：`src/main/resources/assets/apricityui/apricity/...`
2. 游戏实例：`apricity/...`
3. 模组资源包：`assets/apricityui/apricity/...`

推荐使用**相对路径**。以 `/` 开头会从 Apricity 资源根开始解析，不是浏览器站点根目录。

### 9.2 CSS / JS 加载

```html
<style src="panel.css"></style>
<style>/* inline */</style>
<script src="panel.js"></script>
<script>// inline</script>
```

- 外链 CSS 支持 `@import`，走异步加载
- 外链 JS 走本地资源解析，非浏览器脚本标签行为

### 9.3 图片与字体

- 图片：PNG、JPG、GIF（GIF 真的会动）
- 字体：`@font-face` 加载 TTF/OTF，内置 lxgw 精简字体（3500 常用汉字）

### 9.4 远程资源

支持远程图片、CSS、字体（仅 HTTPS，异步加载，有大小和类型限制）。不适合远程 HTML 或远程 JS。

### 9.5 热重载

按 **END** 键重载静态资源并刷新 Document。调试模式下自动监听 `.html` `.css` `.js` 文件变动。

## 10. 尺寸与缩放建议

Minecraft 默认 GUI 缩放下，可用的 GUI 像素尺寸大约是 **427 × 240**。它和浏览器预览比起来会明显偏"大"，也更容易显得拥挤。

- 推荐比浏览器设计稿更克制一点
- 间距、字号、圆角、阴影都别下手太重
- 如果用户没给尺寸，优先做中小号布局，不要一上来铺满整个屏幕
- 尤其是弹窗、卡片、工具面板，宁可小一点，也别做成网页后台管理系统
- 世界内 UI 默认缩放约 0.02f，别拿浏览器大面板尺寸直接套

## 11. 输出协议

- 默认直接输出完整代码
- 推荐顺序：`<body>...</body>` → `<style>...</style>` → `<script>...</script>`
- 资源路径使用相对路径，或从 Apricity 根开始的绝对路径
- 如果用户要求"只要代码"，不要附带解释

## 12. 输出前自检

- 有没有误用浏览器默认布局？
- 有没有写进当前并不可靠的 CSS 或 JS API？
- `recipe` 是否使用了 `type` + `innerText`？
- virtual `slot` 是否由 `innerText` 提供物品？
- 容器标题是否仍在用旧写法？（应为首个子元素文本）
- Screen 绑定场景：顶层 `container` 的 `id` 是否与 `OpenBindPlan` 对齐？
- `querySelectorAll` / `children` 等返回数组，是否避免了 NodeList 专属方法？
- 整份结果能不能直接贴进 ApricityUI 跑？

## 13. 超出能力时的回退策略

- 不要伪造"看起来像能跑"的代码
- 用最接近可运行的降级实现代替
- 如果用户允许解释，再说明哪里做了降级
