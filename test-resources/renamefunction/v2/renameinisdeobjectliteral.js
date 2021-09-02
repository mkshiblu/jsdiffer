THREE.SVGLoader.prototype = {

	constructor: THREE.SVGLoader,

	load: function ( url, onLoad, onProgress, onError ) {

		var scope = this;

		var loader = new THREE.FileLoader( scope.manager );
		loader.load( url, function ( text ) {

			onLoad( scope.parse( text ) );

		}, onProgress, onError );
	},

	parse: function ( text ) {

		function parseNode( node, style ) {

			if ( node.nodeType !== 1 ) return;

			switch ( node.nodeName ) {

				case 'svg':
					break;

				case 'g':
					style = parseStyle( node, style );
					break;

				default:
					console.log( node );

			}

			var nodes = node.childNodes;

			for ( var i = 0; i < nodes.length; i ++ ) {

				parseNode( nodes[ i ], style );

			}

		}

		function parsePathNode( node, style ) {

			var path = new THREE.ShapePath();
			path.color.setStyle( style.fill );

			var point = new THREE.Vector2();
			var control = new THREE.Vector2();

			var d = node.getAttribute( 'd' );

			// console.log( d );

			var commands = d.match( /[a-df-z][^a-df-z]*/ig );

			for ( var i = 0, l = commands.length; i < l; i ++ ) {

				var command = commands[ i ];

				var type = command.charAt( 0 );
				var data = command.substr( 1 ).trim();

				switch ( type ) {

					case 'M':
						var numbers = parseFloats( data );
						for ( var j = 0, jl = numbers.length; j < jl; j += 2 ) {
							point.x = numbers[ j + 0 ];
							point.y = numbers[ j + 1 ];
							control.x = point.x;
							control.y = point.y;
							path.moveTo( point.x, point.y );
						}
						break;

					case 'A':
						var numbers = parseFloats( data );
						for ( var j = 0, jl = numbers.length; j < jl; j += 7 ) {

							point.x = numbers[ j + 5 ];
							point.y = numbers[ j + 6 ];
							var radius = { x: numbers[ j ], y: numbers[ j + 1 ] };
							var x_axis_rotation = numbers[ j + 2 ] * Math.PI / 180;
							svgEllipsisToThreeEllipsis( path, control, radius, x_axis_rotation, numbers[ j + 3 ], numbers[ j + 4 ], point );
							control.x = point.x;
							control.y = point.y;

						}
						break;

					//

					case 'm':
						var numbers = parseFloats( data );
						for ( var j = 0, jl = numbers.length; j < jl; j += 2 ) {
							point.x += numbers[ j + 0 ];
							point.y += numbers[ j + 1 ];
							control.x = point.x;
							control.y = point.y;
							path.moveTo( point.x, point.y );
						}
						break;

					default:
						console.warn( command );

				}

				// console.log( type, parseFloats( data ), parseFloats( data ).length  )

			}

			return path;

		}

		/**
		 * https://www.w3.org/TR/SVG/implnote.html#ArcImplementationNotes
		 * https://mortoray.com/2017/02/16/rendering-an-svg-elliptical-arc-as-bezier-curves/ Appendix: Endpoint to center arc conversion
		 * From
		 * rx ry x-axis-rotation large-arc-flag sweep-flag x y
		 * To
		 * aX, aY, xRadius, yRadius, aStartAngle, aEndAngle, aClockwise, aRotation
		 */
		var vector = new THREE.Vector2();

        function parseArcCommand( path, rx, ry, x_axis_rotation, large_arc_flag, sweep_flag, start, end ) {

            x_axis_rotation = x_axis_rotation * Math.PI / 180;

            // Ensure radii are positive
            var rX = Math.abs( rx );
            var rY = Math.abs( ry );

            // Compute (x1â€², y1â€²)
            var midDist = vector.subVectors( start, end ).multiplyScalar( 0.5 );
            var x1p = Math.cos( x_axis_rotation ) * midDist.x + Math.sin( x_axis_rotation ) * midDist.y;
            var y1p = - Math.sin( x_axis_rotation ) * midDist.x + Math.cos( x_axis_rotation ) * midDist.y;

            // Compute (cxâ€², cyâ€²)
            var rxs = rX * rX;
            var rys = rY * rY;
            var x1ps = x1p * x1p;
            var y1ps = y1p * y1p;

            // Ensure radii are large enough
            var cr = x1ps / rxs + y1ps / rys;
            if ( cr > 1 ) {
                // scale up rX,rY equally so cr == 1
                var s = Math.sqrt( cr );
                rX = s * rX;
                rY = s * rY;
                rxs = rX * rX;
                rys = rY * rY;
            }

            var dq = ( rxs * y1ps + rys * x1ps );
            var pq = ( rxs * rys - dq ) / dq;
            var q = Math.sqrt( pq );
            if ( large_arc_flag === sweep_flag ) q = - q;
            var cxp = q * rX * y1p / rY;
            var cyp = - q * rY * x1p / rX;

            // Step 3: Compute (cx, cy) from (cxâ€², cyâ€²)
            var cx = Math.cos( x_axis_rotation ) * cxp - Math.sin( x_axis_rotation ) * cyp + ( start.x + end.x ) / 2;
            var cy = Math.sin( x_axis_rotation ) * cxp + Math.cos( x_axis_rotation ) * cyp + ( start.y + end.y ) / 2;

            // Step 4: Compute Î¸1 and Î”Î¸
            var startAngle = vector.set( ( x1p - cxp ) / rX, ( y1p - cyp ) / rY ).angle();
            var endAngle = vector.set( ( - x1p - cxp ) / rX, ( - y1p - cyp ) / rY ).angle();
            if ( ! sweep_flag ) endAngle -= 2 * Math.PI;

            path.currentPath.absellipse( cx, cy, rX, rY, startAngle, endAngle, endAngle > startAngle, x_axis_rotation );

        }
    }
};
