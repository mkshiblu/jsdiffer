var x = {
  drawCaret: function (tooltipPoint, size, opacity) {
    var vm = this._view;
    var ctx = this._chart.ctx;
    var x1, x2, x3;
    var y1, y2, y3;
    var caretSize = vm.caretSize;
    var cornerRadius = vm.cornerRadius;
    var xAlign = vm.xAlign,
      yAlign = vm.yAlign;
    var ptX = tooltipPoint.x,
      ptY = tooltipPoint.y;
    var width = size.width,
      height = size.height;

    if (yAlign === 'center') {
      // Left or right side
      if (xAlign === 'left') {
        x1 = ptX;
        x2 = x1 - caretSize;
        x3 = x1;
      } else {
        x1 = ptX + width;
        x2 = x1 + caretSize;
        x3 = x1;
      }

      y2 = ptY + (height / 2);
      y1 = y2 - caretSize;
      y3 = y2 + caretSize;
    } else {
      if (xAlign === 'left') {
        x1 = ptX + cornerRadius;
        x2 = x1 + caretSize;
        x3 = x2 + caretSize;
      } else if (xAlign === 'right') {
        x1 = ptX + width - cornerRadius;
        x2 = x1 - caretSize;
        x3 = x2 - caretSize;
      } else {
        x2 = ptX + (width / 2);
        x1 = x2 - caretSize;
        x3 = x2 + caretSize;
      }

      if (yAlign === 'top') {
        y1 = ptY;
        y2 = y1 - caretSize;
        y3 = y1;
      } else {
        y1 = ptY + height;
        y2 = y1 + caretSize;
        y3 = y1;
      }
    }

    ctx.fillStyle = mergeOpacity(vm.backgroundColor, opacity);
    ctx.lineWidth = vm.borderWidth;
    ctx.strokeStyle = vm.borderColor;

    ctx.beginPath();
    ctx.moveTo(x1, y1);
    ctx.lineTo(x2, y2);
    ctx.lineTo(x3, y3);
    ctx.stroke();
    ctx.fill();
    ctx.closePath();

    helpers.drawRoundedRectangle(ctx, ptX, ptY, size.width, size.height, vm.cornerRadius);
    ctx.fill();
  },
  drawTitle: function (pt, vm, ctx, opacity) {
    var title = vm.title;
  }
};
