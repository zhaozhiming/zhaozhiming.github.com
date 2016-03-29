---
layout: post
title: "使用AVA和Enzyme测试React组件（三）"
date: 2016-03-29 20:42
description: 使用AVA和Enzyme测试React组件（三）
keywords: ava,enzyme,react
comments: true
categories: test
tags: [ava,enzyme,react]
---

{% img /images/post/2016/03/react.png %}  
  
**React组件的测试要点**：React组件要怎么测试，有哪些需要注意的地方，今天我们通过一些例子来说明。  
  
<!--more-->  

## render逻辑的测试
  
React中存在逻辑的地方有一部分是在render方法中，React通过props或state的值可以render出不同的页面，所以我们可以通过设置不同的props值来测试是否能render出我们期望的页面。比如有下面这样的一个组件：  
  
{% codeblock lang:js %}
class Footer extends Component {
  renderFooterButtons(completedCount, clearCompleted) {
    // 测试点1
    if (completedCount > 0) {
      return (
        <button className="clear-completed" onClick={ () => clearCompleted() }>Clear completed</button>
      );
    }
  }

  render() {
    const { todos, actions, onShow } = this.props;
    const { clearCompleted } = actions;
    const activeCount = todos.reduce((count, todo) => todo.completed ? count : count + 1, 0);
    const completedCount = todos.length - activeCount;
    return (
      <footer className="footer">
        <span className="todo-count"><strong>{activeCount}</strong> item left</span>
        <ul className="filters">
          // 测试点2
          {[SHOW_ALL, SHOW_ACTIVE, SHOW_COMPLETED].map(filter =>
            <li key={filter}>
              <a className={classnames({ selected: filter === this.props.filter })}
                style={{ cursor: 'pointer' }}
                onClick={ () => onShow(filter) }>{FILTER_TITLES[filter]}</a>
            </li>
          )}
        </ul>
        {this.renderFooterButtons(completedCount, clearCompleted)}
      </footer>
    );
  }
}
{% endcodeblock %}
  
可以看到这个组件其实是比较简单的，在一个`footer`标签里面有个`span`和一个`ul`，`ul`里面有一些`li`，最下面是个`button`。  
  
* 在最上面有一个render button的方法，这个方法存在逻辑判断，如果completedCount大于0，则render一个button出来，否则不render button，这里是我们第一个测试点。测试代码如下，分别测试render和不render的情况： 
  
{% codeblock lang:js %}
const props = {
  todos: [], // 空的数组
  actions: {
    clearCompleted: sinon.spy(), // mock方法
  },
  onShow: sinon.spy(), // mock方法
  filter: 'SHOW_ALL',
};

test('do not render button', t => {
  const wrapper = shallow(<Footer {...props} />);
  t.is(wrapper.find('button').length, 0);
});

test('render button correctly', t => {
  const wrapper = shallow(<Footer {...props} />);
  wrapper.setProps({ todos: [{ completed: true }] });
  t.is(wrapper.find('button').length, 1);
});
{% endcodeblock %}
  
要让completedCount不大于0，只要给个空的todos集合就可以了，如果要大于0的话，则需要在todos里面添加一个`completed`为true的对象，这里需要搞清楚completedCount的声明逻辑。  
  
* 第二个测试点是map方法里面的逻辑，因为是从一个有3个元素的数组里面做map，所以可以校验是否确实render出来3个`li`，以及其中某个`li`链接的class和文本内容。  
  
{% codeblock lang:js %}
test('render 3 li correctly', t => {
  const wrapper = shallow(<Footer {...props} />);
  wrapper.setProps({ todos: [{ completed: true }] });
  t.is(wrapper.find('li').length, 3);
  t.is(wrapper.find('a.selected').length, 1);
  t.is(wrapper.find('a.selected').text(), 'All');
});
{% endcodeblock %}
  
可以看到通过enzyme的text方法可以很方便地得到`a`标签的文本内容。  
  
这个组件其实还继续做测试，比如`span`里面的render逻辑等，但这里就不详细举例了。  
  
## 组件的事件逻辑
  
除了在render方法中有逻辑以外，在组件的事件中也会存在逻辑，要测试这部分代码，我们需要模拟触发组件的事件。请看下面这个组件：  
  
{% codeblock lang:js %}
class TodoInput extends Component {
  constructor(props, context) {
    super(props, context);
    this.state = {
      text: this.props.text || '',
    };
  }

  handleChange(e) {
    this.setState({ text: e.target.value });
  }

  handleBlur(e) {
    if (!this.props.newTodo) {
      this.props.onSave(e.target.value.trim());
    }
  }

  render() {
    return (
      <input className={
        classnames({
          edit: this.props.editing,
          'new-todo': this.props.newTodo,
        })}
        type="text"
        placeholder={this.props.placeholder}
        autoFocus="true"
        value={this.state.text}
        onBlur={this.handleBlur.bind(this)}
        onChange={this.handleChange.bind(this)}
      />
    );
  }
}
{% endcodeblock %}
  
可以看到这个组件的render方法里面没有什么逻辑，只有一个`input`标签，但是在标签中存在了`change`和`blur`事件，组件的逻辑隐藏在对应的事件方法中。  
  
* 首先是对`change`事件的测试，我们可以看到`handleChange`方法其实是修改state里面text的值，所以测试代码可以这样写：  
  
{% codeblock lang:js %}
const props = {
  text: 'foo',
  placeholder: 'foo placeholder',
  editing: false,
  newTodo: false,
  onSave: sinon.spy(),
};

test('input change value correctly', t => {
  const wrapper = shallow(<TodoInput {...props} />);
  wrapper.find('input').simulate('change', { target: { value: 'bar' } });
  t.is(wrapper.state('text'), 'bar');
});
{% endcodeblock %}
  
通过调用`simulate`方法对`change`事件进行模拟，然后调用`state`方法对组件的state进行校验。  
  
* 接着我们测试`blur`事件，`handleBlur`方法先做判断，如果为真则调用props中的`onSave`方法，我们可以用sinon来mock onSave方法，校验其调用次数。  
  
{% codeblock lang:js %}
test('input blur correctly', t => {
  const wrapper = shallow(<TodoInput {...props} />);
  wrapper.find('input').simulate('blur', { target: { value: 'bar' } });
  t.is(props.onSave.callCount, 1);
{% endcodeblock %}
  
模拟事件触发的方法差不多，都是传入事件名和所需的方法对象就可以了，这里校验`onSave`是否被调用了1次。  
  
在写单元测试的时候，有一点要注意的是要避免过度测试，因为测试代码也是需要维护的，如果测试过多过细，那一旦生产代码有所改变，就可能会修改很多测试代码，需要开发人员需要在质量和开发效率上面做好均衡。  
  
