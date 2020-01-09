const fs = require('fs-extra');

fs.removeSync('./dist');
fs.copySync('./public/index.html', './dist/index.html');
fs.copySync('./public/styles', './dist/styles');
