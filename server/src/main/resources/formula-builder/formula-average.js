// returns arithmetic average of array
function formulaAverage(...args) {
	if (args.length===0) return 0;
	let sum = 0;
	for (let arg of args) sum += arg;
	return sum / args.length;
}
