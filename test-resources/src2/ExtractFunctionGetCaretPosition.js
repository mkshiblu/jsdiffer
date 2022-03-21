var x = {
  drawCaret: function (tooltipPoint, size) {
    var ctx = this._chart.ctx;
    var vm = this._view;
    var caretPosition = this.getCaretPosition(tooltipPoint, size, vm);

    ctx.lineTo(caretPosition.x1, caretPosition.y1);
    ctx.lineTo(caretPosition.x2, caretPosition.y2);
    ctx.lineTo(caretPosition.x3, caretPosition.y3);
  },
  getCaretPosition: function (tooltipPoint, size, vm) {
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
      y2 = ptY + (height / 2);

      if (xAlign === 'left') {
        x1 = ptX;
        x2 = x1 - caretSize;
        x3 = x1;

        y1 = y2 + caretSize;
        y3 = y2 - caretSize;
      } else {
        x1 = ptX + width;
        x2 = x1 + caretSize;
        x3 = x1;

        y1 = y2 - caretSize;
        y3 = y2 + caretSize;
      }
    } else {
      if (xAlign === 'left') {
        x2 = ptX + cornerRadius + (caretSize);
        x1 = x2 - caretSize;
        x3 = x2 + caretSize;
      } else if (xAlign === 'right') {
        x2 = ptX + width - cornerRadius - caretSize;
        x1 = x2 - caretSize;
        x3 = x2 + caretSize;
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
        // invert drawing order
        var tmp = x3;
        x3 = x1;
        x1 = tmp;
      }
    }
    return { x1: x1, x2: x2, x3: x3, y1: y1, y2: y2, y3: y3 };
  },
  drawTitle: function (pt, vm, ctx, opacity) {
    var title = vm.title;
  }
};
