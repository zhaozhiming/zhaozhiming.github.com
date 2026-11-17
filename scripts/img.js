'use strict';

const { htmlTag, url_for } = require('hexo-util');

const rUrl = /((([A-Za-z]{3,9}:(?:\/\/)?)(?:[-;:&=+$,\w]+@)?[A-Za-z0-9.-]+|(?:www.|[-;:&=+$,\w]+@)[A-Za-z0-9.-]+)((?:\/[+~%/.\w-_]*)?\??(?:[-+=&;%@.\w_]*)#?(?:[.!/\\w]*))?)/;

// Theme fabric.js reads img.alt and inserts one <span class="caption">.
// Hexo's built-in {% img %} puts the first quoted string into title, not alt.
hexo.extend.tag.register('img', function imgTag(args) {
  const classes = [];
  let src;
  let width;
  let height;
  let title;
  let alt;

  while (args.length > 0) {
    const item = args.shift();
    if (rUrl.test(item) || item.startsWith('/')) {
      src = url_for.call(hexo, item);
      break;
    }
    classes.push(item);
  }

  if (args.length && !/\D+/.test(args[0])) {
    width = args.shift();
    if (args.length && !/\D+/.test(args[0])) {
      height = args.shift();
    }
  }

  if (args.length) {
    title = args.shift();
    alt = args.length ? args.join(' ') : title;
  }

  return htmlTag('img', {
    src,
    class: classes.join(' '),
    width,
    height,
    title,
    alt
  });
});
