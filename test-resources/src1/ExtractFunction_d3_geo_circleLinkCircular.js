(function() {
  var d3_format_decimalPoint = ".", d3_format_thousandsSeparator = ",", d3_format_grouping = [ 3, 3 ];
  function d3_geo_circleClipPolygon(coordinates, context, clipLine, interpolate, angle) {
    clip.sort(function(a, b) {
        return b.angle - a.angle;
      });
      [ subject, clip ].forEach(function(intersections) {
        for (var i = 0, a = intersections[0], b; i < intersections.length; ) {
          a.next = b = intersections[++i % intersections.length];
          b.prev = a;
          a = b;
        }
      });
      if (!subject.length) return;
  }
})();