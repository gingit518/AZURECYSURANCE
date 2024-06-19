// returns a number with maximum frequency in array (if several numbers have same frequency -> return the biggest of these numbers)
function formulaModeMax(...args) {
	const obj = {};
	args.forEach(number => {
		if (!obj[number]) {
			obj[number] = 1;
		} else {
			obj[number] += 1;
		}
	});
	let highestValue = 0;
	let highestValueKey = -Infinity;
	for (let key in obj) {
		const value = obj[key];
		if (value >= highestValue && Number(key) > highestValueKey) {
			highestValue = value;
			highestValueKey = Number(key);
		}
	}
	return highestValueKey;
}

