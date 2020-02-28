const fs = require('fs-extra');

fs.copySync('public/index.html', 'dist/index.html');
fs.copySync('public/fonts', 'dist/fonts');
fs.copySync('public/img', 'dist/img');
fs.copySync('public/manifest', 'dist/manifest');
